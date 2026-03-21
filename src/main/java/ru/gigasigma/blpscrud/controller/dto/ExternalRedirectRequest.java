package ru.gigasigma.blpscrud.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ExternalRedirectRequest(
        @NotNull(message = "userId обязателен")
        @Positive(message = "userId должен быть положительным числом")
        Long userId,

        @NotNull(message = "flightId обязателен")
        @Positive(message = "flightId должен быть положительным числом")
        Long flightId,

        @NotBlank(message = "currency обязательна")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency должна быть в формате ISO 4217, например RUB")
        String currency,

        @NotBlank(message = "iataCode обязателен")
        @Size(max = 3, message = "iataCode должен быть не длиннее 3 символов")
        String iataCode
) {
}
