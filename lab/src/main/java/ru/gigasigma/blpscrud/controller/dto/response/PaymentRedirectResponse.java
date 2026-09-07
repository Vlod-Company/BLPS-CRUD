package ru.gigasigma.blpscrud.controller.dto.response;

public record PaymentRedirectResponse(
        String redirectUrl,
        String paymentSessionId
) {
}
