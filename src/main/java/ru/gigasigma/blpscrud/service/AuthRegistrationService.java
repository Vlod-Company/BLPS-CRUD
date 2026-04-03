package ru.gigasigma.blpscrud.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.security.XmlAccount;
import ru.gigasigma.blpscrud.security.XmlUserStore;

@Service
@RequiredArgsConstructor
public class AuthRegistrationService {

    private static final String USER_ROLE = "ROLE_USER";

    private final XmlUserStore xmlUserStore;
    private final PasswordEncoder passwordEncoder;

    public XmlAccount register(String login, String password) {
        if (xmlUserStore.existsByLogin(login)) {
            throw new IllegalArgumentException("Login is already taken");
        }
        return xmlUserStore.createUser(login, passwordEncoder.encode(password), USER_ROLE, login);
    }
}
