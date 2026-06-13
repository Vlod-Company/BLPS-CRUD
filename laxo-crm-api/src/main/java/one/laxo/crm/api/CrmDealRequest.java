package one.laxo.crm.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CrmDealRequest(
        String title,
        BigDecimal amount,
        String currency,
        Long externalOrderId,
        String orderStatus,
        String paymentMethod,
        String flightNumber,
        String route,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        String seatNumber,
        String seatClass,
        Boolean hasBaggage,
        String airlineName,
        String contactId
) {
}
