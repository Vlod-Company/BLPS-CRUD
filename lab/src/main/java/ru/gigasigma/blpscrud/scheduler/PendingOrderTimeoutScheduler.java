package ru.gigasigma.blpscrud.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.service.PendingOrderCleanupService;

@Component
@RequiredArgsConstructor
public class PendingOrderTimeoutScheduler {

    private final PendingOrderCleanupService cleanupService;

    @Scheduled(fixedDelayString = "300000")
    public void refreshFlights() {
        cleanupService.cancelExpiredPendingOrders();
    }
}
