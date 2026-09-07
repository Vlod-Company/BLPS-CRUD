package ru.gigasigma.blpscrud.controller.dto.response;

public record RedirectResponse(
        String redirectUrl,
        String bookingSessionId
) {
}
