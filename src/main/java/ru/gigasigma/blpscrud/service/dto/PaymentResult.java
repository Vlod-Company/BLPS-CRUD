package ru.gigasigma.blpscrud.service.dto;

public record PaymentResult(
        boolean success,
        String message
) {
}
