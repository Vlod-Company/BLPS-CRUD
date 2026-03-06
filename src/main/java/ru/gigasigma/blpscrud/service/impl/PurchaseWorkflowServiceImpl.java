package ru.gigasigma.blpscrud.service.impl;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gigasigma.blpscrud.controller.dto.ExternalBookingCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.ExternalRedirectRequest;
import ru.gigasigma.blpscrud.controller.dto.PaymentCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.PaymentRedirectResponse;
import ru.gigasigma.blpscrud.controller.dto.ProcessPaymentRequest;
import ru.gigasigma.blpscrud.controller.dto.RedirectResponse;
import ru.gigasigma.blpscrud.controller.dto.StartPurchaseRequest;
import ru.gigasigma.blpscrud.entity.Flight;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.entity.Ticket;
import ru.gigasigma.blpscrud.entity.User;
import ru.gigasigma.blpscrud.enums.OrderStatus;
import ru.gigasigma.blpscrud.enums.PaymentMethod;
import ru.gigasigma.blpscrud.repository.FlightRepository;
import ru.gigasigma.blpscrud.repository.OrderRepository;
import ru.gigasigma.blpscrud.repository.TicketRepository;
import ru.gigasigma.blpscrud.repository.UserRepository;
import ru.gigasigma.blpscrud.service.AirlineBookingService;
import ru.gigasigma.blpscrud.service.FlightSyncService;
import ru.gigasigma.blpscrud.service.PaymentService;
import ru.gigasigma.blpscrud.service.PurchaseWorkflowService;
import ru.gigasigma.blpscrud.service.TicketDeliveryService;
import ru.gigasigma.blpscrud.service.TicketPricingService;
import ru.gigasigma.blpscrud.service.dto.PaymentResult;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

@Service
@RequiredArgsConstructor
public class PurchaseWorkflowServiceImpl implements PurchaseWorkflowService {

    private final UserRepository userRepository;
    private final FlightRepository flightRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final TicketPricingService ticketPricingService;
    private final FlightSyncService flightSyncService;
    private final PaymentService paymentService;
    private final AirlineBookingService airlineBookingService;
    private final TicketDeliveryService ticketDeliveryService;

    @Override
    @Transactional
    public WorkflowResult startInternalPurchase(StartPurchaseRequest request) {
        Order order = createOrderWithTicket(request, PaymentMethod.INTERNAL, null);
        return toResult(order, "Order and ticket created. Waiting for payment");
    }

    @Override
    @Transactional
    public WorkflowResult startExternalPurchase(StartPurchaseRequest request) {
        Flight flight = flightSyncService.refreshFlightForPurchase(request.flightId());
        String externalLink = buildAirlineRedirectUrl(
                flight,
                "bookingSession=" + UUID.randomUUID() + "&source=internal-start"
        );
        Order order = createOrderWithTicket(request, PaymentMethod.EXTERNAL, externalLink);
        return toResult(order, "Redirect user to external link for payment");
    }

    @Override
    public RedirectResponse generateRedirectLink(ExternalRedirectRequest request) {
        userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.userId()));
        Flight flight = flightSyncService.refreshFlightForPurchase(request.flightId());

        String sessionId = UUID.randomUUID().toString();
        String redirectUrl = buildAirlineRedirectUrl(flight, "session=" + sessionId
                + "&userId=" + request.userId()
                + "&flightId=" + request.flightId()
                + "&currency=" + request.currency());
        return new RedirectResponse(redirectUrl, sessionId);
    }

    @Override
    @Transactional
    public WorkflowResult completeExternalBooking(ExternalBookingCallbackRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.userId()));
        Flight flight = flightSyncService.refreshFlightForPurchase(request.flightId());

        if (flight.getAvailableSeats() <= 0) {
            throw new IllegalStateException("No seats available for flight: " + flight.getId());
        }
        if (ticketRepository.existsByFlightIdAndSeatNumber(flight.getId(), request.seatNumber())) {
            throw new IllegalStateException("Seat already booked: " + request.seatNumber());
        }

        BigDecimal expectedPrice = ticketPricingService.calculateTotalPrice(
                flight.getBasePrice(),
                request.seatClass(),
                Boolean.TRUE.equals(request.hasBaggage())
        );
        if (request.paidAmount() != null && request.paidAmount().compareTo(expectedPrice) < 0) {
            throw new IllegalStateException("Paid amount is less than expected price");
        }

        String externalLink = buildAirlineRedirectUrl(flight, "externalBookingId=" + request.externalBookingId());
        Order order = Order.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .totalPrice(expectedPrice)
                .currency(request.currency())
                .status(OrderStatus.PAID)
                .paymentMethod(PaymentMethod.EXTERNAL)
                .externalLink(externalLink)
                .build();
        Order savedOrder = orderRepository.save(order);

        Ticket ticket = Ticket.builder()
                .flight(flight)
                .order(savedOrder)
                .seatNumber(request.seatNumber())
                .seatClass(request.seatClass())
                .hasBaggage(Boolean.TRUE.equals(request.hasBaggage()))
                .passengerName(request.passengerName())
                .passengerPassport(request.passengerPassport())
                .build();
        Ticket savedTicket = ticketRepository.save(ticket);

        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        flightRepository.save(flight);
        ticketDeliveryService.sendTicket(savedOrder, savedTicket);

        String message = "External booking completed. externalPaymentId="
                + request.externalPaymentId()
                + ", airlineTicketNumber=" + request.airlineTicketNumber()
                + ", passengerEmail=" + request.passengerEmail()
                + ", passengerPhone=" + request.passengerPhone();
        return toResult(savedOrder, message);
    }

    @Override
    public PaymentRedirectResponse generatePaymentRedirectLink(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getPaymentMethod() != PaymentMethod.INTERNAL) {
            throw new IllegalStateException("Payment redirect is available only for INTERNAL orders");
        }
        assertPending(order);

        String sessionId = UUID.randomUUID().toString();
        String redirectUrl = "https://payment-gateway.local/checkout"
                + "?session=" + sessionId
                + "&orderId=" + order.getId()
                + "&amount=" + order.getTotalPrice()
                + "&currency=" + order.getCurrency();
        return new PaymentRedirectResponse(redirectUrl, sessionId);
    }

    @Override
    @Transactional
    public WorkflowResult handlePaymentCallback(PaymentCallbackRequest request) {
        Order order = getOrder(request.orderId());
        if (order.getPaymentMethod() != PaymentMethod.INTERNAL) {
            throw new IllegalStateException("Order is not INTERNAL");
        }
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("Canceled order cannot be paid");
        }

        if (!request.success()) {
            String reason = request.failureReason() == null || request.failureReason().isBlank()
                    ? "Payment failed"
                    : request.failureReason();
            return toResult(order, reason);
        }

        if (order.getStatus() == OrderStatus.PAID) {
            return toResult(order, "Order already paid");
        }
        assertPending(order);
        if (request.paidAmount() != null && request.paidAmount().compareTo(order.getTotalPrice()) < 0) {
            throw new IllegalStateException("Paid amount is less than order total");
        }

        Ticket ticket = getOrderTicket(order.getId());
        airlineBookingService.issueTicket(order, ticket);
        order.setStatus(OrderStatus.PAID);
        Order saved = orderRepository.save(order);
        ticketDeliveryService.sendTicket(saved, ticket);
        return toResult(saved, "Payment callback success. externalPaymentId=" + request.externalPaymentId());
    }

    @Override
    @Transactional
    public WorkflowResult processInternalPayment(Long orderId, ProcessPaymentRequest paymentRequest) {
        Order order = getOrder(orderId);
        if (order.getPaymentMethod() != PaymentMethod.INTERNAL) {
            throw new IllegalStateException("Order is not INTERNAL");
        }
        assertPending(order);

        PaymentResult paymentResult = paymentService.process(order, paymentRequest);
        if (!paymentResult.success()) {
            return toResult(order, paymentResult.message());
        }

        Ticket ticket = getOrderTicket(orderId);
        airlineBookingService.issueTicket(order, ticket);
        order.setStatus(OrderStatus.PAID);
        Order saved = orderRepository.save(order);
        ticketDeliveryService.sendTicket(saved, ticket);
        return toResult(saved, "Payment successful. Ticket issued and sent");
    }

    @Override
    @Transactional
    public WorkflowResult confirmExternalPayment(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getPaymentMethod() != PaymentMethod.EXTERNAL) {
            throw new IllegalStateException("Order is not EXTERNAL");
        }
        assertPending(order);

        Ticket ticket = getOrderTicket(orderId);
        airlineBookingService.issueTicket(order, ticket);
        order.setStatus(OrderStatus.PAID);
        Order saved = orderRepository.save(order);
        ticketDeliveryService.sendTicket(saved, ticket);
        return toResult(saved, "External payment confirmed. Ticket issued and sent");
    }

    @Override
    @Transactional
    public WorkflowResult cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() == OrderStatus.PAID) {
            throw new IllegalStateException("Paid order cannot be canceled in this step");
        }
        if (order.getStatus() == OrderStatus.CANCELED) {
            return toResult(order, "Order is already canceled");
        }

        Ticket ticket = getOrderTicket(orderId);
        Flight flight = ticket.getFlight();
        flight.setAvailableSeats(flight.getAvailableSeats() + 1);
        flightRepository.save(flight);
        order.setStatus(OrderStatus.CANCELED);
        Order saved = orderRepository.save(order);
        return toResult(saved, "Order canceled");
    }

    private Order createOrderWithTicket(StartPurchaseRequest request, PaymentMethod paymentMethod, String externalLink) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.userId()));

        Flight flight = flightSyncService.refreshFlightForPurchase(request.flightId());

        if (flight.getAvailableSeats() <= 0) {
            throw new IllegalStateException("No seats available for flight: " + flight.getId());
        }
        if (ticketRepository.existsByFlightIdAndSeatNumber(flight.getId(), request.seatNumber())) {
            throw new IllegalStateException("Seat already booked: " + request.seatNumber());
        }

        Order order = Order.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .totalPrice(ticketPricingService.calculateTotalPrice(
                        flight.getBasePrice(),
                        request.seatClass(),
                        Boolean.TRUE.equals(request.hasBaggage())
                ))
                .currency(request.currency())
                .status(OrderStatus.PENDING)
                .paymentMethod(paymentMethod)
                .externalLink(externalLink)
                .build();

        Order savedOrder = orderRepository.save(order);

        Ticket ticket = Ticket.builder()
                .flight(flight)
                .order(savedOrder)
                .seatNumber(request.seatNumber())
                .seatClass(request.seatClass())
                .hasBaggage(Boolean.TRUE.equals(request.hasBaggage()))
                .passengerName(request.passengerName())
                .passengerPassport(request.passengerPassport())
                .build();
        ticketRepository.save(ticket);

        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        flightRepository.save(flight);

        return savedOrder;
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    }

    private Ticket getOrderTicket(Long orderId) {
        return ticketRepository.findFirstByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found for order: " + orderId));
    }

    private void assertPending(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in PENDING status");
        }
    }

    private WorkflowResult toResult(Order order, String message) {
        return new WorkflowResult(
                order.getId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCurrency(),
                message,
                order.getExternalLink()
        );
    }

    private String buildAirlineRedirectUrl(Flight flight, String queryPart) {
        String baseUrl = flight.getAirline().getWebsiteUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Airline website URL is not configured for " + flight.getAirline().getIataCode());
        }
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + queryPart;
    }
}
