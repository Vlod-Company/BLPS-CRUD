package ru.gigasigma.blpscrud.controller.dto.request;

import jakarta.validation.constraints.*;

public record ExternalRedirectRequest(
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