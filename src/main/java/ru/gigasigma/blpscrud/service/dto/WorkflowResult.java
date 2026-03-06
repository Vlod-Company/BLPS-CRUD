package ru.gigasigma.blpscrud.service.dto;

import java.math.BigDecimal;
import ru.gigasigma.blpscrud.enums.OrderStatus;

public record WorkflowResult(
        Long orderId,
        OrderStatus status,
        BigDecimal totalPrice,
        String currency,
        String message,
        String externalLink
) {
}
