package ru.gigasigma.blpscrud.service.crm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.laxo.crm.api.CrmPurchaseExportRequest;
import one.laxo.crm.api.CrmPurchaseExportResult;
import one.laxo.crm.api.LaxoCrmConnectionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.entity.Airline;
import ru.gigasigma.blpscrud.entity.Flight;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.entity.Ticket;

@Slf4j
@Service
@RequiredArgsConstructor
public class LaxoCrmExportService {

    private final ObjectProvider<LaxoCrmConnectionFactory> connectionFactoryProvider;

    public void exportSuccessfulPurchase(Order order, Ticket ticket) {
        log.info("Attempting to export order to Laxo CRM. orderId={}", order.getId());

        LaxoCrmConnectionFactory connectionFactory = connectionFactoryProvider.getIfAvailable();
        if (connectionFactory == null) {
            log.warn("Laxo CRM JCA export is disabled. orderId={}", order.getId());
            return;
        }

        CrmPurchaseExportRequest request = mapPurchase(order, ticket);
        try (var connection = connectionFactory.getConnection()) {
            CrmPurchaseExportResult result = connection.exportTicketPurchase(request);
            log.info("{}", result);
            if (result.success()) {
                log.info("Order exported to Laxo CRM. orderId={}, contactId={}, dealId={}",
                        order.getId(), result.contactId(), result.dealId());
            } else {
                log.warn("Laxo CRM export failed. orderId={}, message={}", order.getId(), result.message());
            }
        } catch (RuntimeException e) {
            log.warn("Laxo CRM export failed. orderId={}", order.getId(), e);
        }
    }

    private CrmPurchaseExportRequest mapPurchase(Order order, Ticket ticket) {
        Flight flight = ticket.getFlight();
        Airline airline = flight.getAirline();

        return new CrmPurchaseExportRequest(
                order.getId(),
                order.getUserId(),
                order.getCreatedAt(),
                order.getTotalPrice(),
                order.getCurrency(),
                order.getStatus().name(),
                order.getPaymentMethod().name(),
                order.getExternalLink(),
                ticket.getId(),
                ticket.getSeatNumber(),
                ticket.getSeatClass().name(),
                ticket.getHasBaggage(),
                ticket.getPassengerName(),
                ticket.getPassengerPassport(),
                flight.getId(),
                flight.getFlightNumber(),
                flight.getDepartureAirport(),
                flight.getArrivalAirport(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getAircraftType(),
                flight.getBasePrice(),
                airline.getName(),
                airline.getIataCode(),
                airline.getCountry(),
                airline.getWebsiteUrl()
        );
    }
}
