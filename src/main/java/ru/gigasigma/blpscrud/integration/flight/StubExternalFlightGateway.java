package ru.gigasigma.blpscrud.integration.flight;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.entity.Flight;
import ru.gigasigma.blpscrud.repository.FlightRepository;

@Component
@Primary
@RequiredArgsConstructor
public class StubExternalFlightGateway implements ExternalFlightGateway {

    private final FlightRepository flightRepository;

    @Override
    public List<ExternalFlightSnapshot> fetchFlights() {
        return flightRepository.findAll().stream()
                .map(this::toSnapshot)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ExternalFlightSnapshot> fetchFlightByNumber(String flightNumber) {
        return flightRepository.findByFlightNumber(flightNumber)
                .map(this::toSnapshot);
    }

    private ExternalFlightSnapshot toSnapshot(Flight flight) {
        return new ExternalFlightSnapshot(
                flight.getFlightNumber(),
                flight.getAirline().getName(),
                flight.getAirline().getIataCode(),
                flight.getAirline().getCountry(),
                flight.getDepartureAirport(),
                flight.getArrivalAirport(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getAircraftType(),
                flight.getBasePrice(),
                flight.getAvailableSeats()
        );
    }
}
