package ru.gigasigma.blpscrud.camunda.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("issueTicketDelegate")
public class IssueTicketDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        execution.setVariable("ticketIssued", true);
        log.info("Camunda issue ticket step completed. processInstanceId={}", execution.getProcessInstanceId());
    }
}
