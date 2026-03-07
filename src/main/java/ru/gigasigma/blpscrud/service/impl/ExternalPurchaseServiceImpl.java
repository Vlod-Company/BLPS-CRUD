package ru.gigasigma.blpscrud.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gigasigma.blpscrud.controller.dto.ExternalBookingCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.ExternalRedirectRequest;
import ru.gigasigma.blpscrud.controller.dto.RedirectResponse;
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
import ru.gigasigma.blpscrud.service.ExternalPurchaseService;
import ru.gigasigma.blpscrud.service.FlightSyncService;
import ru.gigasigma.blpscrud.service.TicketDeliveryService;
import ru.gigasigma.blpscrud.service.TicketPricingService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;
import ru.gigasigma.blpscrud.util.PurchaseUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExternalPurchaseServiceImpl implements ExternalPurchaseService {

    private final UserRepository userRepository;
    private final FlightRepository flightRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final TicketPricingService ticketPricingService;
    private final FlightSyncService flightSyncService;
    private final TicketDeliveryService ticketDeliveryService;
    private final PurchaseUtil purchaseUtil;

    @Override
    public RedirectResponse generateRedirectLink(ExternalRedirectRequest request) {
        userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + request.userId()));
        Flight flight = flightSyncService.refreshFlightForPurchase(request.flightId());

        String sessionId = UUID.randomUUID().toString();
        String redirectUrl = purchaseUtil.buildAirlineRedirectUrl(flight, "session=" + sessionId
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
                flight.getBasePrice(), request.seatClass(), Boolean.TRUE.equals(request.hasBaggage()));
        if (request.paidAmount() != null && request.paidAmount().compareTo(expectedPrice) < 0) {
            throw new IllegalStateException("Paid amount is less than expected price");
        }

        String externalLink = purchaseUtil.buildAirlineRedirectUrl(flight, "externalBookingId=" + request.externalBookingId());
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
        return purchaseUtil.toResult(savedOrder, message);
    }
}
