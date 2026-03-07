package ru.gigasigma.blpscrud.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gigasigma.blpscrud.controller.dto.OrderResponse;
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
import ru.gigasigma.blpscrud.service.FlightSyncService;
import ru.gigasigma.blpscrud.service.OrderManagementService;
import ru.gigasigma.blpscrud.service.TicketPricingService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;
import ru.gigasigma.blpscrud.util.PurchaseUtil;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderManagementService {

    private final UserRepository userRepository;
    private final FlightSyncService flightSyncService;
    private final TicketRepository ticketRepository;
    private final TicketPricingService ticketPricingService;
    private final OrderRepository orderRepository;
    private final FlightRepository flightRepository;
    private final PurchaseUtil purchaseUtil;

    public Order createOrderWithTicket(StartPurchaseRequest request, PaymentMethod paymentMethod, String externalLink) {
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

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
    }

    public Ticket getOrderTicket(Long orderId) {
        return ticketRepository.findFirstByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found for order: " + orderId));
    }

    public void assertPending(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in PENDING status");
        }
    }

    public List<OrderResponse> findAllByUserId(Long userId) {
        return orderRepository.findAllByUserId(userId)
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public WorkflowResult cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() == OrderStatus.PAID) {
            throw new IllegalStateException("Paid order cannot be canceled in this step");
        }
        if (order.getStatus() == OrderStatus.CANCELED) {
            return purchaseUtil.toResult(order, "Order is already canceled");
        }

        Ticket ticket = getOrderTicket(orderId);
        Flight flight = ticket.getFlight();
        flight.setAvailableSeats(flight.getAvailableSeats() + 1);
        flightRepository.save(flight);
        order.setStatus(OrderStatus.CANCELED);
        Order saved = orderRepository.save(order);
        return purchaseUtil.toResult(saved, "Order canceled");
    }
}
