package ru.gigasigma.blpscrud.controller.dto;

import ru.gigasigma.blpscrud.enums.SeatClass;

public record StartPurchaseRequest(
        Long userId,
        Long flightId,
        String currency,
        String seatNumber,
        SeatClass seatClass,
        Boolean hasBaggage,
        String passengerName,
        String passengerPassport,
        String provider
) {
}
