package ru.gigasigma.blpscrud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.controller.dto.request.PaymentCallbackRequest;
import ru.gigasigma.blpscrud.service.internalPurchase.InternalPurchaseService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCallbackProcessingService {

    private final InternalPurchaseService internalPurchaseService;
    private final RuntimeService runtimeService;

    public void handleCallback(PaymentCallbackRequest request) {
        log.info("Payment callback received for orderId={}", request.orderId());
        try {
            runtimeService.createMessageCorrelation("PaymentReceived")
                    .processInstanceVariableEquals("orderId", request.orderId())
                    .setVariable("paymentSuccess", request.success())
                    .setVariable("externalPaymentId", request.externalPaymentId())
                    .setVariable("failureReason", request.failureReason())
                    .setVariable("paidAmount", request.paidAmount())
                    .correlate();
            log.info("Payment callback correlated to Camunda process. orderId={}", request.orderId());
        } catch (MismatchingMessageCorrelationException e) {
            log.info("No Camunda process waits for payment callback. Falling back to legacy handler. orderId={}", request.orderId());
            internalPurchaseService.handlePaymentCallback(request);
        }
    }
}
