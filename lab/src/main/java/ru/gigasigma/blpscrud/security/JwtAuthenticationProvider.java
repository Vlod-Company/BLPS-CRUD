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
import ru.gigasigma.blpscrud.service.TokenRevocationService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final XmlUserStore xmlUserStore;
    private final JwtService jwtService;
    private final NetworkPoliticsRepository politicsRepository;
    private final TokenRevocationService tokenRevocationService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return null;
        }

        String token = jwtAuth.getCredentials().toString();
        String requestIp = jwtAuth.getDetails() instanceof String ip ? ip : null;

        try {
            if (!jwtService.isTokenValid(token)) {
                throw new BadCredentialsException("Invalid JWT signature or expired");
            }

            String login = jwtService.extractUsername(token);
            Long tokenPolicyVersion = jwtService.extractPolicyVersion(token);
            String tokenId = jwtService.extractTokenId(token);
            String tokenClientIp = jwtService.extractClientIp(token);

            if (tokenRevocationService.isRevoked(tokenId)) {
                throw new BadCredentialsException("Token has been revoked");
            }

            if (requestIp == null || tokenClientIp == null) {
                throw new BadCredentialsException("Token is missing client IP binding");
            }

            if (!tokenClientIp.equals(requestIp)) {
                tokenRevocationService.revoke(tokenId);
                throw new BadCredentialsException("Token revoked due to client IP change");
            }

            var userDetails = xmlUserStore.findByLogin(login);

            if (userDetails.isEmpty()) {
                throw new BadCredentialsException("User not found");
            }

            long currentPolicyEpoch = politicsRepository.findMaxUpdatedAt()
                    .map(this::toEpochMillis)
                    .orElse(0L);

            if (tokenPolicyVersion == null || tokenPolicyVersion < currentPolicyEpoch) {
                throw new BadCredentialsException("Token expired due to policy update");
            }

            return new JwtAuthenticationToken(userDetails.get().login(), List.of(new SimpleGrantedAuthority(userDetails.get().role())), token);

        } catch (Exception e) {
            throw new BadCredentialsException("JWT validation failed", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private long toEpochMillis(LocalDateTime value) {
        return value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
