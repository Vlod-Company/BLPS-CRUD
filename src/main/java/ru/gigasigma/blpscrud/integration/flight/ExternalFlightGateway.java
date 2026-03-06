package ru.gigasigma.blpscrud.integration.flight;

import java.util.List;
import java.util.Optional;

public interface ExternalFlightGateway {

    List<ExternalFlightSnapshot> fetchFlights();

    Optional<ExternalFlightSnapshot> fetchFlightByNumber(String flightNumber);
}
