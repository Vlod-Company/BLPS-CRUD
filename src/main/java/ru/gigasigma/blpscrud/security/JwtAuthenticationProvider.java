package ru.gigasigma.blpscrud.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.repository.NetworkPoliticsRepository;
import ru.gigasigma.blpscrud.service.JwtService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final XmlUserStore xmlUserStore;
    private final JwtService jwtService;
    private final NetworkPoliticsRepository politicsRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return null;
        }

        String token = jwtAuth.getCredentials().toString();

        try {
            if (!jwtService.isTokenValid(token)) {
                throw new BadCredentialsException("Invalid JWT signature or expired");
            }

            String login = jwtService.extractUsername(token);
            Long tokenPolicyVersion = jwtService.extractPolicyVersion(token);

            var userDetails = xmlUserStore.findByLogin(login);

            if (userDetails.isEmpty()) {
                throw new BadCredentialsException("User not found");
            }

            LocalDateTime currentPolicyVersion = politicsRepository.findMaxUpdatedAt().get();
            long currentPolicyEpoch = currentPolicyVersion.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

            if (tokenPolicyVersion == null || tokenPolicyVersion < currentPolicyEpoch) {
                throw new BadCredentialsException("Token expired due to policy update");
            }

            return new JwtAuthenticationToken(userDetails, List.of(new SimpleGrantedAuthority(userDetails.get().role())), token);

        } catch (Exception e) {
            throw new BadCredentialsException("JWT validation failed", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
