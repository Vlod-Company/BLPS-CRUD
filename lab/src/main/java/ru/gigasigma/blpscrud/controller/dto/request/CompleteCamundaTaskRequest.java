package ru.gigasigma.blpscrud.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(name = "CompleteCamundaTaskRequest", description = "Variables submitted when completing a Camunda user task")
public record CompleteCamundaTaskRequest(
        @Schema(description = "Process variables to set before completing the task")
        Map<String, Object> variables
) {
}
