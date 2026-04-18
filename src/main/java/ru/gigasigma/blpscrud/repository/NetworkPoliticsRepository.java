package ru.gigasigma.blpscrud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.gigasigma.blpscrud.entity.NetworkPolitics;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NetworkPoliticsRepository extends JpaRepository<NetworkPolitics, Long> {

    Optional<NetworkPolitics> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT MAX(p.updatedAt) FROM NetworkPolitics p")
    Optional<LocalDateTime> findMaxUpdatedAt();
}