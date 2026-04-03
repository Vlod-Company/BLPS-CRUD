package ru.gigasigma.blpscrud.service.flightSync.impl;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import ru.gigasigma.blpscrud.controller.dto.response.PaymentRedirectResponse;
import ru.gigasigma.blpscrud.entity.Airline;
import ru.gigasigma.blpscrud.entity.Flight;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.enums.PaymentMethod;
import ru.gigasigma.blpscrud.integration.flight.ExternalFlightGateway;
import ru.gigasigma.blpscrud.integration.flight.ExternalFlightSnapshot;
import ru.gigasigma.blpscrud.repository.AirlineRepository;
import ru.gigasigma.blpscrud.repository.FlightRepository;
import ru.gigasigma.blpscrud.service.flightSync.FlightSyncService;
import ru.gigasigma.blpscrud.transaction.ProgrammaticTransaction;

@Service
@RequiredArgsConstructor
public class FlightSyncServiceImpl implements FlightSyncService {

    private final FlightRepository flightRepository;
    private final AirlineRepository airlineRepository;
    private final ExternalFlightGateway externalFlightGateway;
    private final PlatformTransactionManager txManager;

    @Override
    public void refreshCatalog() {
        ProgrammaticTransaction.defaultTransactionVoid(txManager, TransactionDefinition.withDefaults(),
        () -> {
            List<ExternalFlightSnapshot> snapshots = externalFlightGateway.fetchFlights();
            snapshots.forEach(this::upsertFlightFromSnapshot);
        });
    }

    @Override
    public Flight refreshFlightForPurchase(Long flightId) {
        return ProgrammaticTransaction.defaultTransaction(txManager, TransactionDefinition.withDefaults(),
        () -> {
            Flight localFlight = flightRepository.findById(flightId)
                    .orElseThrow(() -> new EntityNotFoundException("Flight not found: " + flightId));

            return externalFlightGateway.fetchFlightByNumber(localFlight.getFlightNumber())
                    .map(this::upsertFlightFromSnapshot)
                    .orElse(localFlight);
        });
    }

    private Flight upsertFlightFromSnapshot(ExternalFlightSnapshot snapshot) {
        Airline airline = airlineRepository.findByIataCode(snapshot.airlineIataCode())
                .orElseGet(() -> airlineRepository.save(Airline.builder()
                        .name(snapshot.airlineName())
                        .iataCode(snapshot.airlineIataCode())
                        .country(snapshot.airlineCountry())
                        .websiteUrl(snapshot.airlineWebsiteUrl())
                        .build()));
        airline.setWebsiteUrl(snapshot.airlineWebsiteUrl());
        airlineRepository.save(airline);

        Flight flight = flightRepository.findByFlightNumber(snapshot.flightNumber())
                .orElseGet(Flight::new);

        flight.setFlightNumber(snapshot.flightNumber());
        flight.setAirline(airline);
        flight.setDepartureAirport(snapshot.departureAirport());
        flight.setArrivalAirport(snapshot.arrivalAirport());
        flight.setDepartureTime(snapshot.departureTime());
        flight.setArrivalTime(snapshot.arrivalTime());
        flight.setAircraftType(snapshot.aircraftType());
        flight.setBasePrice(snapshot.basePrice());
        flight.setAvailableSeats(snapshot.availableSeats());

        return flightRepository.save(flight);
    }
}
