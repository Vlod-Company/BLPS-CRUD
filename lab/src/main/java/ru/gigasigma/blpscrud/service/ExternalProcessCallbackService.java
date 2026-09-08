package ru.gigasigma.blpscrud.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.gigasigma.blpscrud.controller.dto.request.ExternalBookingCallbackRequest;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

@Service
@RequiredArgsConstructor
public class ExternalProcessCallbackService {
    private static final String REDIRECT_MESSAGE = "RedirectReceived";

    private final RuntimeService runtimeService;
    private final ObjectMapper objectMapper;

    @Transactional
    public WorkflowResult complete(String session, ExternalBookingCallbackRequest request) {
        if (session == null || session.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "External booking session is required");
        }
        try {
            var result = runtimeService.createMessageCorrelation(REDIRECT_MESSAGE)
                    .processInstanceVariableEquals("externalSessionId", session)
                    .processInstanceVariableEquals("userId", request.userId())
                    .processInstanceVariableEquals("flightId", request.flightId())
                    .processInstanceVariableEquals("currency", request.currency())
                    .setVariable("externalBookingCallback", objectMapper.writeValueAsString(request))
                    .correlateWithResultAndVariables(false);
            Object payload = result.getVariables().get("externalBookingResult");
            if (!(payload instanceof String json)) {
                throw new IllegalStateException("External purchase process did not produce a booking result");
            }
            return objectMapper.readValue(json, WorkflowResult.class);
        } catch (MismatchingMessageCorrelationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No matching purchase is waiting for this callback; check the session and payment-link task", exception);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot process external booking payload", exception);
        }
    }
}
