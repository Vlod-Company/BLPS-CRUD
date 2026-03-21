package ru.gigasigma.blpscrud.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.gigasigma.blpscrud.enums.SeatClass;

public record StartPurchaseRequest(
        @NotNull(message = "userId обязателен")
        @Positive(message = "userId должен быть положительным числом")
        Long userId,

        @NotNull(message = "flightId обязателен")
        @Positive(message = "flightId должен быть положительным числом")
        Long flightId,

        @NotBlank(message = "currency обязательна")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency должна быть в формате ISO 4217, например RUB")
        String currency,

        @NotBlank(message = "seatNumber обязателен")
        @Pattern(regexp = "^[1-9][0-9]{0,2}[A-F]$", message = "seatNumber должен быть в формате типа 12A")
        String seatNumber,

        @NotNull(message = "seatClass обязателен")
        SeatClass seatClass,

        @NotNull(message = "hasBaggage обязателен")
        Boolean hasBaggage,

        @NotBlank(message = "passengerName обязателен")
        @Size(min = 2, max = 100, message = "passengerName должен быть длиной от 2 до 100 символов")
        @Pattern(regexp = "^\\p{L}[\\p{L}\\-\\s']{1,99}$", message = "passengerName содержит недопустимые символы")
        String passengerName,

        @NotBlank(message = "passengerPassport обязателен")
        @Pattern(regexp = "^[A-Za-z0-9\\- ]{6,20}$", message = "passengerPassport должен содержать от 6 до 20 символов")
        String passengerPassport,

        @NotBlank(message = "provider обязателен")
        @Size(max = 32, message = "provider должен быть не длиннее 32 символов")
        String provider
) {
}
