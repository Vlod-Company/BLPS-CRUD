package ru.gigasigma.blpscrud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigasigma.blpscrud.entity.RevokedToken;

import java.time.LocalDateTime;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

    boolean existsByTokenId(String tokenId);

    long deleteByRevokedAtBefore(LocalDateTime threshold);
}
