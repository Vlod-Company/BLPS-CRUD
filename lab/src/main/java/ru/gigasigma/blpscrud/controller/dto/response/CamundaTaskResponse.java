package ru.gigasigma.blpscrud.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.camunda.bpm.engine.task.Task;

@Schema(name = "CamundaTaskResponse", description = "Camunda user task")
public record CamundaTaskResponse(
        String id,
        String name,
        String taskDefinitionKey,
        String processInstanceId,
        String processDefinitionId,
        String assignee,
        LocalDateTime createdAt
) {

    public static CamundaTaskResponse from(Task task) {
        return new CamundaTaskResponse(
                task.getId(),
                task.getName(),
                task.getTaskDefinitionKey(),
                task.getProcessInstanceId(),
                task.getProcessDefinitionId(),
                task.getAssignee(),
                task.getCreateTime() == null ? null : task.getCreateTime().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDateTime()
        );
    }
}
