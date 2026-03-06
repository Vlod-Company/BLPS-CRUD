package ru.gigasigma.blpscrud.service;

import java.time.LocalDateTime;
import java.util.List;
import ru.gigasigma.blpscrud.entity.Flight;

public interface FlightQueryService {

    List<Flight> search(String from, String to, LocalDateTime departureFrom, LocalDateTime departureTo, Integer passengers);

    Flight getById(Long id);
}
