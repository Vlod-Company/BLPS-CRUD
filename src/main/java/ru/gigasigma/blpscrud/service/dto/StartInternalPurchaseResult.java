package ru.gigasigma.blpscrud.service.dto;

import ru.gigasigma.blpscrud.enums.OrderStatus;

import java.math.BigDecimal;

public record StartInternalPurchaseResult(
        Long orderId,
        OrderStatus status,
        BigDecimal totalPrice,
        String currency,
        String message,
        String paymentRedirectUrl,
        String sessionId
) {
}
