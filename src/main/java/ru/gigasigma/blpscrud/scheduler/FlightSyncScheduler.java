package ru.gigasigma.blpscrud.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.service.FlightSyncService;

@Component
@RequiredArgsConstructor
public class FlightSyncScheduler {

    private final FlightSyncService flightSyncService;

    @Scheduled(fixedDelayString = "${flight.sync.fixed-delay-ms:900000}")
    public void refreshFlights() {
        flightSyncService.refreshCatalog();
    }
}
