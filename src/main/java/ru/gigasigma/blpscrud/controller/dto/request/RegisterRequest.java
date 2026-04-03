package ru.gigasigma.blpscrud.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterRequest", description = "Payload for registering a new application user")
public record RegisterRequest(
        @Schema(description = "Unique login", example = "ivan1")
        @NotBlank(message = "login is required")
        @Size(min = 4, message = "login length must be at least 4")
        String login,

        @Schema(description = "Raw password", example = "pass1234")
        @NotBlank(message = "password is required")
        @Size(min = 4, message = "password length must be at least 4")
        String password
) {
}