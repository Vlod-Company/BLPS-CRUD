package ru.gigasigma.blpscrud.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import ru.gigasigma.blpscrud.entity.Flight;

@Schema(name = "FlightResponse", description = "Public flight representation")
public record FlightResponse(
        @Schema(description = "Flight identifier", example = "105")
        Long id,
        @Schema(description = "Airline flight number", example = "SU1234")
        String flightNumber,
        @Schema(description = "IATA airline code", example = "SU")
        String airlineCode,
        @Schema(description = "Departure airport IATA code", example = "LED")
        String departureAirport,
        @Schema(description = "Arrival airport IATA code", example = "SVO")
        String arrivalAirport,
        @Schema(description = "Departure timestamp", example = "2026-03-24T09:20:00")
        LocalDateTime departureTime,
        @Schema(description = "Arrival timestamp", example = "2026-03-24T10:45:00")
        LocalDateTime arrivalTime,
        @Schema(description = "Aircraft type", example = "Airbus A320")
        String aircraftType,
        @Schema(description = "Base price for one passenger", example = "12450.00")
        BigDecimal basePrice,
        @Schema(description = "Number of seats still available", example = "37")
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
