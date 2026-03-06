package ru.gigasigma.blpscrud.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigasigma.blpscrud.entity.Airline;

public interface AirlineRepository extends JpaRepository<Airline, Long> {
    Optional<Airline> findByIataCode(String iataCode);
    boolean existsByIataCode(String iataCode);
}
