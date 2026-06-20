package ru.gigasigma.blpscrud.camunda.delegate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.service.PendingOrderCleanupService;

@Slf4j
@Component("cancelPendingOrdersDelegate")
@RequiredArgsConstructor
public class CancelPendingOrdersDelegate implements JavaDelegate {

    private final PendingOrderCleanupService cleanupService;

    @Override
    public void execute(DelegateExecution execution) {
        int canceledCount = cleanupService.cancelExpiredPendingOrders();
        execution.setVariable("canceledPendingOrders", canceledCount);
        log.info("Camunda pending order cleanup finished. canceledCount={}", canceledCount);
    }
}
