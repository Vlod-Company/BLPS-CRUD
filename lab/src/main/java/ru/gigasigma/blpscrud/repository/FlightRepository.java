package ru.gigasigma.blpscrud.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigasigma.blpscrud.entity.Flight;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    Optional<Flight> findByFlightNumber(String flightNumber);
    List<Flight> findAllByAirlineId(Long airlineId);
    List<Flight> findAllByDepartureTimeBetween(LocalDateTime from, LocalDateTime to);
    List<Flight> findAllByDepartureAirportAndArrivalAirport(String departureAirport, String arrivalAirport);
}
