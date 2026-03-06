package ru.gigasigma.blpscrud.controller.dto;

public record RedirectResponse(
        String redirectUrl,
        String bookingSessionId
) {
}
