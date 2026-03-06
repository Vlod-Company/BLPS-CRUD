package ru.gigasigma.blpscrud.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import ru.gigasigma.blpscrud.entity.Flight;

public record FlightResponse(
        Long id,
        String flightNumber,
        String airlineCode,
        String departureAirport,
        String arrivalAirport,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        String aircraftType,
        BigDecimal basePrice,
        Integer availableSeats
) {
    public static FlightResponse fromEntity(Flight flight) {
        return new FlightResponse(
                flight.getId(),
                flight.getFlightNumber(),
                flight.getAirline().getIataCode(),
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
