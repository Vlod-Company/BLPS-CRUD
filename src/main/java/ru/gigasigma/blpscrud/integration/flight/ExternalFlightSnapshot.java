package ru.gigasigma.blpscrud.integration.flight;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExternalFlightSnapshot(
        String flightNumber,
        String airlineName,
        String airlineIataCode,
        String airlineCountry,
        String departureAirport,
        String arrivalAirport,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        String aircraftType,
        BigDecimal basePrice,
        Integer availableSeats
) {
}
