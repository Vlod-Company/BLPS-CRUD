package ru.gigasigma.blpscrud.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.gigasigma.blpscrud.entity.User;

@Schema(name = "RegisterResponse", description = "Newly created user summary")
public record RegisterResponse(
        @Schema(description = "User identifier", example = "42")
        Long id,

        @Schema(description = "User login", example = "ivan1")
        String login,

        @Schema(description = "Assigned security role", example = "ROLE_USER")
        String role
) {
    public static RegisterResponse fromEntity(User user) {
        return new RegisterResponse(user.getId(), user.getLogin(), "ROLE_USER");
    }
}