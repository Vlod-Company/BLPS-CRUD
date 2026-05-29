package ru.gigasigma.blpscrud.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.service.externalAirlineLogic.ExternalPurchaseService;
import ru.gigasigma.blpscrud.service.externalAirlineLogic.impl.ExternalPurchaseServiceImpl;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ExternalPurchaseServiceFactory {

    private final Map<String, ExternalPurchaseService> externalPurchaseServices;
    private final ExternalPurchaseServiceImpl externalPurchaseServiceImpl;

    public ExternalPurchaseService getService(String provider) {
        return externalPurchaseServices.getOrDefault(provider.toUpperCase(), externalPurchaseServiceImpl);
    }
}
