package ru.gigasigma.blpscrud.controller.dto;

import java.math.BigDecimal;
import ru.gigasigma.blpscrud.enums.SeatClass;

public record ExternalBookingCallbackRequest(
        Long userId,
        Long flightId,
        String currency,
        String seatNumber,
        SeatClass seatClass,
        Boolean hasBaggage,
        String passengerName,
        String passengerPassport,
        String passengerEmail,
        String passengerPhone,
        String externalPaymentId,
        String externalBookingId,
        String airlineTicketNumber,
        BigDecimal paidAmount
) {
}
