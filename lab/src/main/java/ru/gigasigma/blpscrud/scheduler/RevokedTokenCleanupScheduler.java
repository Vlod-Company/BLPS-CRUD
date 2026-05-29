package ru.gigasigma.blpscrud.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.service.TokenRevocationService;

@Component
@RequiredArgsConstructor
public class RevokedTokenCleanupScheduler {

    private final TokenRevocationService tokenRevocationService;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    @Scheduled(fixedDelayString = "${jwt.revoked-token-cleanup.fixed-delay-ms:3600000}")
    public void purgeExpiredRevokedTokens() {
        tokenRevocationService.purgeExpiredTokens(jwtExpiration);
    }
}
