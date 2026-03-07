package ru.gigasigma.blpscrud.util;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.controller.dto.StartPurchaseRequest;
import ru.gigasigma.blpscrud.entity.Flight;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.entity.Ticket;
import ru.gigasigma.blpscrud.entity.User;
import ru.gigasigma.blpscrud.enums.OrderStatus;
import ru.gigasigma.blpscrud.enums.PaymentMethod;
import ru.gigasigma.blpscrud.repository.FlightRepository;
import ru.gigasigma.blpscrud.repository.OrderRepository;
import ru.gigasigma.blpscrud.repository.TicketRepository;
import ru.gigasigma.blpscrud.repository.UserRepository;
import ru.gigasigma.blpscrud.service.FlightSyncService;
import ru.gigasigma.blpscrud.service.TicketPricingService;
import ru.gigasigma.blpscrud.service.dto.StartInternalPurchaseResult;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

import java.time.LocalDateTime;

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
