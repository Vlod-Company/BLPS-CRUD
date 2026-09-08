package ru.gigasigma.blpscrud.camunda.delegate;

import static ru.gigasigma.blpscrud.camunda.CamundaVariables.longValue;
import static ru.gigasigma.blpscrud.camunda.CamundaVariables.stringValue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.controller.dto.request.ExternalRedirectRequest;
import ru.gigasigma.blpscrud.service.externalAirlineLogic.ExternalPurchaseService;

@Slf4j
@Component("redirectDelegate")
@RequiredArgsConstructor
public class RedirectDelegate implements JavaDelegate {

    private final ExternalPurchaseService externalPurchaseService;

    @Override
    public void execute(DelegateExecution execution) {
        var request = new ExternalRedirectRequest(longValue(execution, "flightId"),
                stringValue(execution, "currency"), stringValue(execution, "provider"));
        var redirect = externalPurchaseService.generateRedirectLink(request, longValue(execution, "userId"));
        if (redirect == null || redirect.redirectUrl() == null || redirect.redirectUrl().isBlank()
                || redirect.bookingSessionId() == null || redirect.bookingSessionId().isBlank()) {
            throw new IllegalStateException("Airline did not return a redirect URL and session ID");
        }
        execution.setVariable("redirectUrl", redirect.redirectUrl());
        execution.setVariable("externalSessionId", redirect.bookingSessionId());
        execution.setVariable("redirectPrepared", true);
        log.info("Camunda external redirect prepared. processInstanceId={}", execution.getProcessInstanceId());
    }
}
