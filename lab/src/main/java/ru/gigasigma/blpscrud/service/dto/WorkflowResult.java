package ru.gigasigma.blpscrud.service.dto;

import ru.gigasigma.blpscrud.enums.OrderStatus;

import java.math.BigDecimal;

public record WorkflowResult(
        Long orderId,
        OrderStatus status,
        BigDecimal totalPrice,
        String currency,
        String message,
        String externalLink
) {
}
