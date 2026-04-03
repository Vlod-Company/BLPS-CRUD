package ru.gigasigma.blpscrud.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.entity.Flight;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

@Component
@RequiredArgsConstructor
public class PurchaseUtil {

    public WorkflowResult toResult(Order order, String message) {
        return new WorkflowResult(
                order.getId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCurrency(),
                message,
                order.getExternalLink()
        );
    }

    public String buildAirlineRedirectUrl(Flight flight, String queryPart) {
        String baseUrl = flight.getAirline().getWebsiteUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Airline website URL is not configured for " + flight.getAirline().getIataCode());
        }
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + queryPart + "&returnUrl=/callback";
    }
}
