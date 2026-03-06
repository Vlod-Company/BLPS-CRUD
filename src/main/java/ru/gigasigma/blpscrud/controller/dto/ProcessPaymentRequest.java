package ru.gigasigma.blpscrud.controller.dto;

public record ProcessPaymentRequest(
        String cardNumber,
        String cardHolder,
        String cvv,
        boolean forceFail
) {
}
