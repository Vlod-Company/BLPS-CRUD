package ru.gigasigma.blpscrud.camunda;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.camunda.bpm.engine.ProcessEngineException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import ru.gigasigma.blpscrud.controller.ApiExceptionHandler;

class ProcessValidationResponseTest {
    @Test
    void returnsOnlyValidationMessageAndDoesNotMaskEngineFailures() {
        var handler = new ApiExceptionHandler();
        var request = new MockHttpServletRequest("POST", "/api/process/tasks/task/complete");
        var failure = new ProcessEngineException("ENGINE wrapper",
                new PurchaseFormValidationException("Choose a flight", null));

        var response = handler.handleProcessValidation(failure, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("Choose a flight");
        var databaseFailure = new ProcessEngineException("Database failure");
        assertThatThrownBy(() -> handler.handleProcessValidation(databaseFailure, request))
                .isSameAs(databaseFailure);
    }
}
