package ru.gigasigma.blpscrud.camunda.delegate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.camunda.CamundaVariables;
import ru.gigasigma.blpscrud.controller.dto.request.ExternalBookingCallbackRequest;
import ru.gigasigma.blpscrud.service.externalAirlineLogic.ExternalPurchaseService;

@Slf4j
@Component("externalPurchaseDelegate")
@RequiredArgsConstructor
public class ExternalPurchaseDelegate implements JavaDelegate {
    private final ExternalPurchaseService purchases;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Override
    public void execute(DelegateExecution execution) {
        String payload = CamundaVariables.stringValue(execution, "externalBookingCallback");
        if (payload == null) {
            throw new IllegalArgumentException("External booking callback is required");
        }
        ExternalBookingCallbackRequest request;
        try {
            request = objectMapper.readValue(payload, ExternalBookingCallbackRequest.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid external booking callback", exception);
        }
        if (request == null || !validator.validate(request).isEmpty()) {
            throw new IllegalArgumentException("External booking callback contains invalid fields");
        }
        if (!request.userId().equals(CamundaVariables.longValue(execution, "userId"))
                || !request.flightId().equals(CamundaVariables.longValue(execution, "flightId"))) {
            throw new IllegalArgumentException("External booking does not match the process owner or flight");
        }
        var result = purchases.completeExternalBookingForProcess(request);
        try {
            execution.setVariable("externalBookingResult", objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize external booking result", exception);
        }
        execution.setVariable("orderId", result.orderId());
        execution.setProcessBusinessKey(result.orderId().toString());
        execution.setVariable("externalPurchaseCompleted", true);
        log.info("Camunda external purchase completed. processInstanceId={}, orderId={}",
                execution.getProcessInstanceId(), result.orderId());
    }
}
