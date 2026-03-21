package ru.gigasigma.blpscrud.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.enums.OrderStatus;
import ru.gigasigma.blpscrud.enums.PaymentMethod;

@Schema(name = "OrderResponse", description = "Public order representation")
public record OrderResponse(
        @Schema(description = "Order identifier", example = "501")
        Long id,
        @Schema(description = "Owner user identifier", example = "42")
        Long userId,
        @Schema(description = "Creation timestamp", example = "2026-03-21T11:45:00")
        LocalDateTime createdAt,
        @Schema(description = "Total order price", example = "15450.00")
        BigDecimal totalPrice,
        @Schema(description = "ISO 4217 currency code", example = "RUB")
        String currency,
        @Schema(description = "Current order status", example = "PENDING")
        OrderStatus status,
        @Schema(description = "Payment processing mode", example = "INTERNAL")
        PaymentMethod paymentMethod,
        @Schema(description = "Optional external link for payment or booking", example = "https://partner.example.com/session/abc123")
        String externalLink
) {
    public static OrderResponse fromEntity(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getCreatedAt(),
                order.getTotalPrice(),
                order.getCurrency(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getExternalLink()
        );
    }
}
