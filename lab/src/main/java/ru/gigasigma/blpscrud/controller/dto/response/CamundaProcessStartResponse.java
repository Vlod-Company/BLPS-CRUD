package ru.gigasigma.blpscrud.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(name = "CamundaProcessStartResponse", description = "Started Camunda process instance")
public record CamundaProcessStartResponse(
        String processInstanceId,
        String processDefinitionId,
        String businessKey,
        Map<String, Object> variables
) {
}
