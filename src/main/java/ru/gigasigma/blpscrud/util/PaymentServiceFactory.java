package ru.gigasigma.blpscrud.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.service.PaymentService;
import ru.gigasigma.blpscrud.service.impl.PaymentServiceImpl;

import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentServiceFactory {

    private final Map<String, PaymentService> paymentServices;
    private final PaymentServiceImpl paymentService;

    public PaymentService getPaymentService(String provider) {
        if (provider == null || provider.isBlank()) {
            return paymentService;
        }
        String key = provider.trim();
        PaymentService byExact = paymentServices.get(key);
        if (byExact != null) {
            return byExact;
        }
        PaymentService byUpper = paymentServices.get(key.toUpperCase(Locale.ROOT));
        if (byUpper != null) {
            return byUpper;
        }
        PaymentService byLower = paymentServices.get(key.toLowerCase(Locale.ROOT));
        if (byLower != null) {
            return byLower;
        }
        return paymentService;
    }
}
