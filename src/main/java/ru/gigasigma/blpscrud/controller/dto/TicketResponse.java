package ru.gigasigma.blpscrud.controller.dto;

import ru.gigasigma.blpscrud.enums.SeatClass;
import ru.gigasigma.blpscrud.entity.Ticket;

public record TicketResponse(
        Long id,
        Long flightId,
        Long orderId,
        String seatNumber,
        SeatClass seatClass,
        Boolean hasBaggage,
        String passengerName,
        String passengerPassport
) {
    public static TicketResponse fromEntity(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getFlight().getId(),
                ticket.getOrder().getId(),
                ticket.getSeatNumber(),
                ticket.getSeatClass(),
                ticket.getHasBaggage(),
                ticket.getPassengerName(),
                ticket.getPassengerPassport()
        );
    }
}
