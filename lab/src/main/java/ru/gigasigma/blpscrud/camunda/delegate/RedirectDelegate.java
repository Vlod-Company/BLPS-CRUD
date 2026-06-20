package ru.gigasigma.blpscrud.camunda.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("redirectDelegate")
public class RedirectDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        execution.setVariable("redirectPrepared", true);
        log.info("Camunda external redirect prepared. processInstanceId={}", execution.getProcessInstanceId());
    }
}
