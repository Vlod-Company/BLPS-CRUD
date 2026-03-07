package ru.gigasigma.blpscrud.controller.dto;

public record ExternalRedirectRequest(
        Long userId,
        Long flightId,
        String currency,
        String provider
) {
}
