package ru.gigasigma.blpscrud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigasigma.blpscrud.entity.Airline;

import java.util.Optional;

public interface AirlineRepository extends JpaRepository<Airline, Long> {
    Optional<Airline> findByIataCode(String iataCode);

}
