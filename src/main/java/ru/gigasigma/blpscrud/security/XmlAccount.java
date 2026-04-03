package ru.gigasigma.blpscrud.security;

public record XmlAccount(
        Long id,
        String login,
        String passwordHash,
        String role,
        String fullName
) {
}
