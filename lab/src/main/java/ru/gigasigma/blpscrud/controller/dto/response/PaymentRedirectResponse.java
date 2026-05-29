package ru.gigasigma.blpscrud.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PaymentRedirectResponse", description = "Redirect metadata returned when an internal payment session is created")
public record PaymentRedirectResponse(
        @Schema(description = "Absolute URL to open in the client", example = "https://pay.example.com/session/pay_abc123")
        String redirectUrl,
        @Schema(description = "Payment session identifier", example = "payment-session-abc123")
        String paymentSessionId
) {
}
