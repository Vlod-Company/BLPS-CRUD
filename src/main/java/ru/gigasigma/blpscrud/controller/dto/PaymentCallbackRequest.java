package ru.gigasigma.blpscrud.controller.dto;

import java.math.BigDecimal;

public record PaymentCallbackRequest(
        Long orderId,
        boolean success,
        String externalPaymentId,
        String failureReason,
        BigDecimal paidAmount
) {
}
