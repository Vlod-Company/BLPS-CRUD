package one.laxo.crm.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CrmPurchaseExportRequest(
        Long orderId,
        Long userId,
        LocalDateTime createdAt,
        BigDecimal totalPrice,
        String currency,
        String orderStatus,
        String paymentMethod,
        String externalLink,
        Long ticketId,
        String seatNumber,
        String seatClass,
        Boolean hasBaggage,
        String passengerName,
        String passengerPassport,
        Long flightId,
        String flightNumber,
        String departureAirport,
        String arrivalAirport,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        String aircraftType,
        BigDecimal basePrice,
        String airlineName,
        String airlineIataCode,
        String airlineCountry,
        String airlineWebsiteUrl
) {
}
