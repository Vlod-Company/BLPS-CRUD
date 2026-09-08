package ru.gigasigma.blpscrud.controller.dto.response;

import ru.gigasigma.blpscrud.security.XmlAccount;

public record RegisterResponse(
        Long id,
        String login,
        String role
) {
    public static RegisterResponse fromAccount(XmlAccount account) {
        return new RegisterResponse(account.id(), account.login(), account.role());
    }
}
