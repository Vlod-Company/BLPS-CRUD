package ru.gigasigma.blpscrud.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import ru.gigasigma.blpscrud.enums.OrderStatus;

@Schema(name = "WorkflowResult", description = "Result of an order, booking, or payment workflow step")
public record WorkflowResult(
        @Schema(description = "Order identifier", example = "501")
        Long orderId,
        @Schema(description = "Current workflow status", example = "PAID")
        OrderStatus status,
        @Schema(description = "Total order price", example = "15450.00")
        BigDecimal totalPrice,
        @Schema(description = "ISO 4217 currency code", example = "RUB")
        String currency,
        @Schema(description = "Human-readable workflow message", example = "Payment confirmed and tickets issued")
        String message,
        @Schema(description = "Optional external payment or booking link", example = "https://partner.example.com/session/abc123")
        String externalLink
) {
}
