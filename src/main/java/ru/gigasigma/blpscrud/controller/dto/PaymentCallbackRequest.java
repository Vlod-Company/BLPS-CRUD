package ru.gigasigma.blpscrud.controller.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentCallbackRequest(
        @NotNull(message = "orderId обязателен")
        @Positive(message = "orderId должен быть положительным числом")
        Long orderId,

        boolean success,

        @NotBlank(message = "externalPaymentId обязателен")
        String externalPaymentId,

        String failureReason,

        @NotNull(message = "paidAmount обязателен")
        @DecimalMin(value = "0.00", inclusive = true, message = "paidAmount не может быть отрицательным")
        BigDecimal paidAmount
) {
    @AssertTrue(message = "Если success=false, необходимо указать failureReason")
    public boolean isFailureReasonValid() {
        return success || (failureReason != null && !failureReason.isBlank());
    }
}
