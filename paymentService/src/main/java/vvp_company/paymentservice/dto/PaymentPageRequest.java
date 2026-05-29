package vvp_company.paymentservice.dto;

import java.math.BigDecimal;

public record PaymentPageRequest(
        String session,
        Long orderId,
        BigDecimal amount,
        String currency,
        String replyTo
) {
}