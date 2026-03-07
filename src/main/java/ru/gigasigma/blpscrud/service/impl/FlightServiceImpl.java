package ru.gigasigma.blpscrud.service.impl;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.entity.Flight;
import ru.gigasigma.blpscrud.repository.FlightRepository;
import ru.gigasigma.blpscrud.service.FlightService;

@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;

    @Override
    public List<Flight> search(String from, String to, LocalDateTime departureFrom, LocalDateTime departureTo, Integer passengers) {
        int requiredSeats = passengers == null || passengers < 1 ? 1 : passengers;
        return flightRepository.findAllByDepartureAirportAndArrivalAirport(from, to)
                .stream()
                .filter(flight -> departureFrom == null || !flight.getDepartureTime().isBefore(departureFrom))
                .filter(flight -> departureTo == null || !flight.getDepartureTime().isAfter(departureTo))
                .filter(flight -> flight.getAvailableSeats() >= requiredSeats)
                .toList();
    }

    @Override
    public Flight getById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Flight not found: " + id));
    }
}
