package ru.gigasigma.blpscrud.security;

public record XmlAccount(
        String login,
        String passwordHash,
        String role
) {
}