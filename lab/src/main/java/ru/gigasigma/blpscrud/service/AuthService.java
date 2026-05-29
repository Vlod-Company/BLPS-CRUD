package ru.gigasigma.blpscrud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.gigasigma.blpscrud.controller.dto.request.UserRequest;
import ru.gigasigma.blpscrud.controller.dto.response.LoginResponse;
import ru.gigasigma.blpscrud.security.XmlAccount;
import ru.gigasigma.blpscrud.security.XmlUserStore;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String USER_ROLE = "ROLE_USER";

    private final XmlUserStore xmlUserStore;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

    public LoginResponse login(UserRequest user, String clientIp) {
        Optional<XmlAccount> xmlUser = xmlUserStore.findByLogin(user.login());

        if (xmlUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (!passwordEncoder.matches(user.password(), xmlUser.get().passwordHash())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return new LoginResponse(jwtService.generateToken(user.login(), clientIp));
    }
}
