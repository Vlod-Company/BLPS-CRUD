package ru.gigasigma.blpscrud.service;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.entity.Flight;
import ru.gigasigma.blpscrud.repository.FlightRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightService {

    private final FlightRepository flightRepository;

    public List<Flight> search(String from, String to, LocalDateTime departureFrom, LocalDateTime departureTo, Integer passengers) {
        int requiredSeats = passengers == null || passengers < 1 ? 1 : passengers;
        List<Flight> flights = flightRepository.findAllByDepartureAirportAndArrivalAirport(from, to)
                .stream()
                .filter(flight -> departureFrom == null || !flight.getDepartureTime().isBefore(departureFrom))
                .filter(flight -> departureTo == null || !flight.getDepartureTime().isAfter(departureTo))
                .filter(flight -> flight.getAvailableSeats() >= requiredSeats)
                .toList();
        log.debug("Flight search repository result. from={}, to={}, requiredSeats={}, matched={}", from, to, requiredSeats, flights.size());
        return flights;
    }

    public Flight getById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flight not found: " + id));
        log.debug("Loaded flight by id. id={}, flightNumber={}, airlineId={}", id, flight.getFlightNumber(), flight.getAirline() == null ? null : flight.getAirline().getId());
        return flight;
    }
}
