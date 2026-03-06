package ru.gigasigma.blpscrud.service.impl;

import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.controller.dto.ProcessPaymentRequest;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.service.PaymentService;
import ru.gigasigma.blpscrud.service.dto.PaymentResult;

@Service
public class StubPaymentService implements PaymentService {

    @Override
    public PaymentResult process(Order order, ProcessPaymentRequest request) {
        if (request.forceFail()) {
            return new PaymentResult(false, "Payment rejected by test flag");
        }
        if (request.cardNumber() == null || request.cardNumber().isBlank()) {
            return new PaymentResult(false, "Card number is required");
        }
        return new PaymentResult(true, "Payment accepted");
    }
}
