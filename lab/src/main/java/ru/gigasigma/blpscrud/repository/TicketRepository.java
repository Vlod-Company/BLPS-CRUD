package ru.gigasigma.blpscrud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigasigma.blpscrud.entity.Ticket;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByOrderUserId(Long userId);
    boolean existsByFlightIdAndSeatNumber(Long flightId, String seatNumber);
    Optional<Ticket> findFirstByOrderId(Long orderId);
    Optional<Ticket> findByIdAndOrderUserId(Long id, Long userId);
}
