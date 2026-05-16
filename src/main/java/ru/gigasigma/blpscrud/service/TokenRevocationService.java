package ru.gigasigma.blpscrud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gigasigma.blpscrud.entity.RevokedToken;
import ru.gigasigma.blpscrud.repository.RevokedTokenRepository;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenRevocationService {

    private final RevokedTokenRepository revokedTokenRepository;

    public boolean isRevoked(String tokenId) {
        return tokenId != null && revokedTokenRepository.existsByTokenId(tokenId);
    }

    @Transactional
    public void revoke(String tokenId) {
        if (tokenId == null || tokenId.isBlank() || revokedTokenRepository.existsByTokenId(tokenId)) {
            return;
        }

        revokedTokenRepository.save(RevokedToken.builder()
                .tokenId(tokenId)
                .build());
    }

    @Transactional
    public void purgeExpiredTokens(long jwtExpiration) {
        purgeExpiredTokens(LocalDateTime.now(), jwtExpiration);
    }

    private void purgeExpiredTokens(LocalDateTime now, long jwtExpiration) {
        LocalDateTime threshold = now.minusNanos(jwtExpiration * 1_000_000L);
        long deleted = revokedTokenRepository.deleteByRevokedAtBefore(threshold);
        if (deleted > 0) {
            log.info("Deleted {} expired revoked tokens older than {}", deleted, threshold);
        }
    }
}
