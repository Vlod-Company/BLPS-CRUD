package ru.gigasigma.blpscrud.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.entity.User;
import ru.gigasigma.blpscrud.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public String getCurrentLogin() {
        Authentication authentication = getAuthentication();
        return authentication.getName();
    }

    public User getCurrentUser() {
        String login = getCurrentLogin();
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found for login: " + login));
    }

    public boolean isAdmin() {
        return getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Authenticated user is required");
        }
        return authentication;
    }
}