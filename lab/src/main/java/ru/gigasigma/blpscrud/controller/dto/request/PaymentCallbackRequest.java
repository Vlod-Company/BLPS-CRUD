package ru.gigasigma.blpscrud.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(name = "PaymentCallbackRequest", description = "Callback payload from an internal payment provider")
public record PaymentCallbackRequest(
        @Schema(description = "Order identifier", example = "501")
        @NotNull(message = "orderId is required")
        @Positive(message = "orderId must be a positive number")
        Long orderId,

        @Schema(description = "Payment result flag", example = "true")
        boolean success,

        @Schema(description = "Provider payment identifier", example = "pay_9f8e7d6c")
        @NotBlank(message = "externalPaymentId is required")
        String externalPaymentId,

        @Schema(description = "Failure reason when success=false", example = "3DS authentication failed")
        String failureReason,

        @Schema(description = "Amount actually paid", example = "15450.00")
        @NotNull(message = "paidAmount is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "paidAmount cannot be negative")
        BigDecimal paidAmount
) {
    @AssertTrue(message = "If success=false, failureReason must be provided")
    public boolean isFailureReasonValid() {
        return success || (failureReason != null && !failureReason.isBlank());
    }
}
