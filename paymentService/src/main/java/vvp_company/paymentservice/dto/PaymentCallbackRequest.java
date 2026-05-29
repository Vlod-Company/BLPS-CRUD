package vvp_company.paymentservice.dto;

import java.math.BigDecimal;

public record PaymentCallbackRequest(
        Long orderId,
        boolean success,
        String externalPaymentId,
        String failureReason,
        BigDecimal paidAmount
) {
}
