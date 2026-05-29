package ru.gigasigma.blpscrud.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.gigasigma.blpscrud.entity.Ticket;
import ru.gigasigma.blpscrud.enums.SeatClass;

@Schema(name = "TicketResponse", description = "Issued ticket details")
public record TicketResponse(
        @Schema(description = "Ticket identifier", example = "901")
        Long id,
        @Schema(description = "Flight identifier", example = "105")
        Long flightId,
        @Schema(description = "Order identifier", example = "501")
        Long orderId,
        @Schema(description = "Seat number", example = "12A")
        String seatNumber,
        @Schema(description = "Cabin class", example = "ECONOMY")
        SeatClass seatClass,
        @Schema(description = "Whether baggage is included", example = "true")
        Boolean hasBaggage,
        @Schema(description = "Passenger full name", example = "Ivan Petrov")
        String passengerName,
        @Schema(description = "Passenger passport or document number", example = "4510 123456")
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
