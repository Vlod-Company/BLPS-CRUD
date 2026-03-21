package ru.gigasigma.blpscrud.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RedirectResponse", description = "Redirect metadata returned by the external booking provider")
public record RedirectResponse(
        @Schema(description = "Absolute URL to open in the client", example = "https://partner.example.com/booking/session/abc123")
        String redirectUrl,
        @Schema(description = "Provider-side booking session identifier", example = "booking-session-abc123")
        String bookingSessionId
) {
}
