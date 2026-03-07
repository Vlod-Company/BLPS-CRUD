package ru.gigasigma.blpscrud.controller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import ru.gigasigma.blpscrud.enums.SeatClass;

public record ExternalBookingCallbackRequest(
        @NotNull(message = "userId is required")
        @Positive(message = "userId must be a positive number")
        Long userId,

        @NotNull(message = "flightId is required")
        @Positive(message = "flightId must be a positive number")
        Long flightId,

        @NotBlank(message = "currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be ISO 4217 format, e.g. RUB")
        String currency,

        @NotBlank(message = "seatNumber is required")
        @Pattern(regexp = "^[1-9][0-9]{0,2}[A-F]$", message = "seatNumber must have format like 12A")
        String seatNumber,

        @NotNull(message = "seatClass is required")
        SeatClass seatClass,

        @NotNull(message = "hasBaggage is required")
        Boolean hasBaggage,

        @NotBlank(message = "passengerName is required")
        @Size(min = 2, max = 100, message = "passengerName length must be between 2 and 100")
        @Pattern(regexp = "^[\\p{L}][\\p{L}\\-\\s']{1,99}$", message = "passengerName contains invalid characters")
        String passengerName,

        @NotBlank(message = "passengerPassport is required")
        @Pattern(
                regexp = "^(?:\\d{4}\\s?\\d{6}|[A-Z]{2}\\d{7}|[A-Z0-9]{6,20})$",
                message = "passengerPassport format is invalid"
        )
        String passengerPassport,

        @NotBlank(message = "passengerEmail is required")
        @Email(message = "passengerEmail must be a valid email")
        String passengerEmail,

        @NotBlank(message = "passengerPhone is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "passengerPhone must have format like +79991234567")
        String passengerPhone,

        @NotBlank(message = "externalPaymentId is required")
        @Size(max = 64, message = "externalPaymentId length must be <= 64")
        String externalPaymentId,

        @NotBlank(message = "externalBookingId is required")
        @Size(max = 64, message = "externalBookingId length must be <= 64")
        String externalBookingId,

        @NotBlank(message = "airlineTicketNumber is required")
        @Size(max = 64, message = "airlineTicketNumber length must be <= 64")
        String airlineTicketNumber,

        @NotNull(message = "paidAmount is required")
        @DecimalMin(value = "0.01", message = "paidAmount must be greater than 0")
        BigDecimal paidAmount
) {
}
