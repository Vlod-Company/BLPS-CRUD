package ru.gigasigma.blpscrud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.security.XmlAccount;
import ru.gigasigma.blpscrud.security.XmlUserStore;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthRegistrationService {

    private static final String USER_ROLE = "ROLE_USER";

    private final XmlUserStore xmlUserStore;
    private final PasswordEncoder passwordEncoder;

    public XmlAccount register(String login, String password) {
        log.info("Registering XML user account. login={}", login);
        if (xmlUserStore.existsByLogin(login)) {
            log.warn("Registration rejected because login already exists. login={}", login);
            throw new IllegalArgumentException("Login is already taken");
        }
        XmlAccount account = xmlUserStore.createUser(login, passwordEncoder.encode(password), USER_ROLE, login);
        log.info("XML user account registered successfully. id={}, login={}", account.id(), account.login());
        return account;
    }
}
