package ru.gigasigma.blpscrud.controller.dto;

public record PaymentRedirectResponse(
        String redirectUrl,
        String paymentSessionId
) {
}
