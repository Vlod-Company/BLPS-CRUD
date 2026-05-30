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
        LaxoCrmConnectionFactory connectionFactory = connectionFactoryProvider.getIfAvailable();
        if (connectionFactory == null) {
            log.debug("Laxo CRM JCA export is disabled. orderId={}", order.getId());
            return;
        }

        CrmPurchaseExportRequest request = mapPurchase(order, ticket);
        try (var connection = connectionFactory.getConnection()) {
            CrmPurchaseExportResult result = connection.exportTicketPurchase(request);
            if (result.isSuccess()) {
                log.info("Order exported to Laxo CRM. orderId={}, contactId={}, dealId={}",
                        order.getId(), result.getContactId(), result.getDealId());
            } else {
                log.warn("Laxo CRM export failed. orderId={}, message={}", order.getId(), result.getMessage());
            }
        } catch (RuntimeException e) {
            log.warn("Laxo CRM export failed. orderId={}", order.getId(), e);
        }
    }

    private CrmPurchaseExportRequest mapPurchase(Order order, Ticket ticket) {
        Flight flight = ticket.getFlight();
        Airline airline = flight.getAirline();

        CrmPurchaseExportRequest request = new CrmPurchaseExportRequest();
        request.setOrderId(order.getId());
        request.setUserId(order.getUserId());
        request.setCreatedAt(order.getCreatedAt());
        request.setTotalPrice(order.getTotalPrice());
        request.setCurrency(order.getCurrency());
        request.setOrderStatus(order.getStatus().name());
        request.setPaymentMethod(order.getPaymentMethod().name());
        request.setExternalLink(order.getExternalLink());
        request.setTicketId(ticket.getId());
        request.setSeatNumber(ticket.getSeatNumber());
        request.setSeatClass(ticket.getSeatClass().name());
        request.setHasBaggage(ticket.getHasBaggage());
        request.setPassengerName(ticket.getPassengerName());
        request.setPassengerPassport(ticket.getPassengerPassport());
        request.setFlightId(flight.getId());
        request.setFlightNumber(flight.getFlightNumber());
        request.setDepartureAirport(flight.getDepartureAirport());
        request.setArrivalAirport(flight.getArrivalAirport());
        request.setDepartureTime(flight.getDepartureTime());
        request.setArrivalTime(flight.getArrivalTime());
        request.setAircraftType(flight.getAircraftType());
        request.setBasePrice(flight.getBasePrice());
        request.setAirlineName(airline.getName());
        request.setAirlineIataCode(airline.getIataCode());
        request.setAirlineCountry(airline.getCountry());
        request.setAirlineWebsiteUrl(airline.getWebsiteUrl());
        return request;
    }
}
