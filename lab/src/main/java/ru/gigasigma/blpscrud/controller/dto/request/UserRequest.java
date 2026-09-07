package ru.gigasigma.blpscrud.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "login is required")
        @Size(min = 4, message = "login length must be at least 4")
        String login,

        @NotBlank(message = "password is required")
        @Size(min = 4, message = "password length must be at least 4")
        String password
) {
}