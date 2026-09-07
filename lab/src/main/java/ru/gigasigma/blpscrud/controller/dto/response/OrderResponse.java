package ru.gigasigma.blpscrud.controller.dto.response;

import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.enums.OrderStatus;
import ru.gigasigma.blpscrud.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        Long userId,
        LocalDateTime createdAt,
        BigDecimal totalPrice,
        String currency,
        OrderStatus status,
        PaymentMethod paymentMethod,
        String externalLink
) {
    public static OrderResponse fromEntity(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getCreatedAt(),
                order.getTotalPrice(),
                order.getCurrency(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getExternalLink()
        );
    }
}
