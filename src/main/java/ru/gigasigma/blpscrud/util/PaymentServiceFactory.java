package ru.gigasigma.blpscrud.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.service.PaymentService;
import ru.gigasigma.blpscrud.service.impl.PaymentServiceImpl;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentServiceFactory {

    private final Map<String, PaymentService> paymentServices;
    private final PaymentServiceImpl paymentService;

    public PaymentService getPaymentService(String provider) {
        return paymentServices.getOrDefault(provider, paymentService);
    }
}
