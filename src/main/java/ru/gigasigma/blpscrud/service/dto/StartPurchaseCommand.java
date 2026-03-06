package ru.gigasigma.blpscrud.service.dto;

import ru.gigasigma.blpscrud.enums.PaymentMethod;
import ru.gigasigma.blpscrud.enums.SeatClass;

public record StartPurchaseCommand(
        Long userId,
        Long flightId,
        PaymentMethod paymentMethod,
        String currency,
        String seatNumber,
        SeatClass seatClass,
        Boolean hasBaggage,
        String passengerName,
        String passengerPassport
) {
}
