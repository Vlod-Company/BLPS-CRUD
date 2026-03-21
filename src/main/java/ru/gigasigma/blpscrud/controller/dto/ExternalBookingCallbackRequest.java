package ru.gigasigma.blpscrud.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import ru.gigasigma.blpscrud.enums.SeatClass;

@Schema(name = "ExternalBookingCallbackRequest", description = "Callback payload received after an external booking is completed")
public record ExternalBookingCallbackRequest(
        @Schema(description = "User identifier", example = "42")
        @NotNull(message = "userId is required")
        @Positive(message = "userId must be a positive number")
        Long userId,

        @Schema(description = "Flight identifier", example = "105")
        @NotNull(message = "flightId is required")
        @Positive(message = "flightId must be a positive number")
        Long flightId,

        @Schema(description = "ISO 4217 currency code", example = "USD")
        @NotBlank(message = "currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be ISO 4217 format, e.g. RUB")
        String currency,

        @Schema(description = "Booked seat number", example = "12A")
        @NotBlank(message = "seatNumber is required")
        @Pattern(regexp = "^[1-9][0-9]{0,2}[A-F]$", message = "seatNumber must have format like 12A")
        String seatNumber,

        @Schema(description = "Cabin class", example = "BUSINESS")
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

        @Schema(description = "Passenger email", example = "ivan.petrov@example.com")
        @NotBlank(message = "passengerEmail is required")
        @Email(message = "passengerEmail must be a valid email")
        String passengerEmail,

        @Schema(description = "Passenger phone number", example = "+79991234567")
        @NotBlank(message = "passengerPhone is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "passengerPhone must have format like +79991234567")
        String passengerPhone,

        @Schema(description = "External payment identifier", example = "pay_9f8e7d6c")
        @NotBlank(message = "externalPaymentId is required")
        @Size(max = 64, message = "externalPaymentId length must be <= 64")
        String externalPaymentId,

        @Schema(description = "External booking identifier", example = "book_123456")
        @NotBlank(message = "externalBookingId is required")
        @Size(max = 64, message = "externalBookingId length must be <= 64")
        String externalBookingId,

        @Schema(description = "Airline ticket number", example = "5551234567890")
        @NotBlank(message = "airlineTicketNumber is required")
        @Size(max = 64, message = "airlineTicketNumber length must be <= 64")
        String airlineTicketNumber,

        @Schema(description = "Amount charged by the provider", example = "15450.00")
        @NotNull(message = "paidAmount is required")
        @DecimalMin(value = "0.01", message = "paidAmount must be greater than 0")
        BigDecimal paidAmount
) {
}
