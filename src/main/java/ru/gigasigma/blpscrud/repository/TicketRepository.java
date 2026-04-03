package ru.gigasigma.blpscrud.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigasigma.blpscrud.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByOrderId(Long orderId);
    List<Ticket> findAllByFlightId(Long flightId);
    boolean existsByFlightIdAndSeatNumber(Long flightId, String seatNumber);
    Optional<Ticket> findFirstByOrderId(Long orderId);
    Optional<Ticket> findByIdAndOrderUserLogin(Long id, String login);
}