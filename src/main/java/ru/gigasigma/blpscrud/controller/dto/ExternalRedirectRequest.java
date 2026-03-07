package ru.gigasigma.blpscrud.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ExternalRedirectRequest(
        @NotNull(message = "userId is required")
        @Positive(message = "userId must be a positive number")
        Long userId,

        @NotNull(message = "flightId is required")
        @Positive(message = "flightId must be a positive number")
        Long flightId,

        @NotBlank(message = "currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be ISO 4217 format, e.g. RUB")
        String currency,

        @NotBlank(message = "provider is required")
        @Size(max = 32, message = "provider length must be <= 32")
        String provider
) {
}
