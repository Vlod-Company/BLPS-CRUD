package ru.gigasigma.blpscrud.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(name = "ExternalRedirectRequest", description = "Payload for generating an external booking redirect")
public record ExternalRedirectRequest(
        @Schema(description = "Flight identifier", example = "105")
        @NotNull(message = "flightId is required")
        @Positive(message = "flightId must be a positive number")
        Long flightId,

        @Schema(description = "ISO 4217 currency code", example = "USD")
        @NotBlank(message = "currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be ISO 4217 format, e.g. RUB")
        String currency,

        @Schema(description = "External provider code", example = "s7")
        @NotBlank(message = "provider is required")
        @Size(max = 32, message = "provider length must be <= 32")
        String provider
) {
}