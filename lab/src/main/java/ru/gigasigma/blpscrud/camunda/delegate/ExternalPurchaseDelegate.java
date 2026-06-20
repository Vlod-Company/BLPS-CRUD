package ru.gigasigma.blpscrud.camunda.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("externalPurchaseDelegate")
public class ExternalPurchaseDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        execution.setVariable("externalPurchaseCompleted", true);
        log.info("Camunda external purchase stub completed. processInstanceId={}", execution.getProcessInstanceId());
    }
}
