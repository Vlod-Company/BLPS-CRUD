package ru.gigasigma.blpscrud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.security.XmlAccount;
import ru.gigasigma.blpscrud.security.XmlUserStore;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentUserService {

    private final XmlUserStore xmlUserStore;

    public String getCurrentLogin() {
        return getAuthentication().getName();
    }

    public Long getCurrentUserId() {
        return getCurrentAccount().id();
    }

    public XmlAccount getCurrentAccount() {
        String login = getCurrentLogin();
        XmlAccount account = xmlUserStore.findByLogin(login)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("User not found for login: " + login));
        log.debug("Resolved current XML account. id={}, login={}, role={}", account.id(), account.login(), account.role());
        return account;
    }

    public boolean isAdmin() {
        boolean isAdmin = getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        log.debug("Current principal admin check. login={}, isAdmin={}", getCurrentLogin(), isAdmin);
        return isAdmin;
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new IllegalStateException("Authenticated user is required");
        }
        return authentication;
    }
}
