package ru.gigasigma.blpscrud.service.impl;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.gigasigma.blpscrud.service.PurchaseWorkflowService;
import ru.gigasigma.blpscrud.service.TicketPricingService;
import ru.gigasigma.blpscrud.service.dto.StartPurchaseCommand;

@Service
@RequiredArgsConstructor
public class PurchaseWorkflowServiceImpl implements PurchaseWorkflowService {

    private final UserRepository userRepository;
    private final FlightRepository flightRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final TicketPricingService ticketPricingService;
    private final FlightSyncService flightSyncService;

    @Override
    @Transactional
    public Order startInternalPurchase(StartPurchaseCommand command) {
        if (command.paymentMethod() != PaymentMethod.INTERNAL) {
            throw new IllegalArgumentException("Only INTERNAL flow is supported in this step");
        }

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + command.userId()));

        Flight flight = flightSyncService.refreshFlightForPurchase(command.flightId());

        if (flight.getAvailableSeats() <= 0) {
            throw new IllegalStateException("No seats available for flight: " + flight.getId());
        }
        if (ticketRepository.existsByFlightIdAndSeatNumber(flight.getId(), command.seatNumber())) {
            throw new IllegalStateException("Seat already booked: " + command.seatNumber());
        }

        Order order = Order.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .totalPrice(ticketPricingService.calculateTotalPrice(
                        flight.getBasePrice(),
                        command.seatClass(),
                        Boolean.TRUE.equals(command.hasBaggage())
                ))
                .currency(command.currency())
                .status(OrderStatus.PENDING)
                .paymentMethod(command.paymentMethod())
                .build();

        Order savedOrder = orderRepository.save(order);

        Ticket ticket = Ticket.builder()
                .flight(flight)
                .order(savedOrder)
                .seatNumber(command.seatNumber())
                .seatClass(command.seatClass())
                .hasBaggage(Boolean.TRUE.equals(command.hasBaggage()))
                .passengerName(command.passengerName())
                .passengerPassport(command.passengerPassport())
                .build();
        ticketRepository.save(ticket);

        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        flightRepository.save(flight);

        return savedOrder;
    }
}
