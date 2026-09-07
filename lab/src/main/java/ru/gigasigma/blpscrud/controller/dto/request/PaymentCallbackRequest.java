package ru.gigasigma.blpscrud.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentCallbackRequest(
        @NotNull(message = "orderId is required")
        @Positive(message = "orderId must be a positive number")
        Long orderId,

        boolean success,

        @NotBlank(message = "externalPaymentId is required")
        String externalPaymentId,

        String failureReason,

        @NotNull(message = "paidAmount is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "paidAmount cannot be negative")
        BigDecimal paidAmount
) {
}
