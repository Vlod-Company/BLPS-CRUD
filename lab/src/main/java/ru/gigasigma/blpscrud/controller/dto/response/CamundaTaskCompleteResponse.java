package ru.gigasigma.blpscrud.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(name = "CamundaTaskCompleteResponse", description = "Result after completing a Camunda user task")
public record CamundaTaskCompleteResponse(
        String processInstanceId,
        boolean processEnded,
        Map<String, Object> variables
) {
}
