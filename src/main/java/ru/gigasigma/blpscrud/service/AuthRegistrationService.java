package ru.gigasigma.blpscrud.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.entity.User;
import ru.gigasigma.blpscrud.repository.UserRepository;
import ru.gigasigma.blpscrud.security.XmlUserStore;

@Service
@RequiredArgsConstructor
public class AuthRegistrationService {

    private static final String USER_ROLE = "ROLE_USER";

    private final UserRepository userRepository;
    private final XmlUserStore xmlUserStore;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(String login, String password) {
        if (userRepository.existsByLogin(login) || xmlUserStore.existsByLogin(login)) {
            throw new IllegalArgumentException("Login is already taken");
        }

        User user = userRepository.save(User.builder()
                .login(login)
                .password(passwordEncoder.encode(password))
                .fullName(login)
                .build());

        xmlUserStore.createUser(login, passwordEncoder.encode(password), USER_ROLE);
        return user;
    }
}