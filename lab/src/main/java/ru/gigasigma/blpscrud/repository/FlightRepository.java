package ru.gigasigma.blpscrud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigasigma.blpscrud.entity.Flight;

import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    Optional<Flight> findByFlightNumber(String flightNumber);
    List<Flight> findAllByDepartureAirportAndArrivalAirport(String departureAirport, String arrivalAirport);
}
