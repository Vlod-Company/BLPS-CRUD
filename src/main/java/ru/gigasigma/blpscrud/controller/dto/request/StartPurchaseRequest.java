package ru.gigasigma.blpscrud.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.gigasigma.blpscrud.enums.SeatClass;

@Schema(name = "StartPurchaseRequest", description = "Payload for creating an internal order and redirecting to payment")
public record StartPurchaseRequest(
        @Schema(description = "Flight identifier", example = "105")
        @NotNull(message = "flightId is required")
        @Positive(message = "flightId must be a positive number")
        Long flightId,

        @Schema(description = "ISO 4217 currency code", example = "RUB")
        @NotBlank(message = "currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be ISO 4217 format, e.g. RUB")
        String currency,

        @Schema(description = "Preferred seat number", example = "12A")
        @NotBlank(message = "seatNumber is required")
        @Pattern(regexp = "^[1-9][0-9]{0,2}[A-F]$", message = "seatNumber must have format like 12A")
        String seatNumber,

        @Schema(description = "Requested cabin class", example = "ECONOMY")
        @NotNull(message = "seatClass is required")
        SeatClass seatClass,

        @Schema(description = "Whether baggage is included", example = "true")
        @NotNull(message = "hasBaggage is required")
        Boolean hasBaggage,

        @Schema(description = "Passenger full name", example = "Ivan Petrov")
        @NotBlank(message = "passengerName is required")
        @Size(min = 2, max = 100, message = "passengerName length must be between 2 and 100")
        @Pattern(regexp = "^[\\p{L}][\\p{L}\\-\\s']{1,99}$", message = "passengerName contains invalid characters")
        String passengerName,

        @Schema(description = "Passenger passport or document number", example = "4510 123456")
        @NotBlank(message = "passengerPassport is required")
        @Pattern(
                regexp = "^(?:\\d{4}\\s?\\d{6}|[A-Z]{2}\\d{7}|[A-Z0-9]{6,20})$",
                message = "passengerPassport format is invalid"
        )
        String passengerPassport,

        @Schema(description = "Payment provider identifier", example = "internal-bank")
        @NotBlank(message = "provider is required")
        @Size(max = 32, message = "provider length must be <= 32")
        String provider
) {
}