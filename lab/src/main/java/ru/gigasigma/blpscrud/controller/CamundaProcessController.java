package ru.gigasigma.blpscrud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gigasigma.blpscrud.controller.dto.request.CompleteCamundaTaskRequest;
import ru.gigasigma.blpscrud.controller.dto.request.StartPurchaseRequest;
import ru.gigasigma.blpscrud.controller.dto.response.CamundaProcessStartResponse;
import ru.gigasigma.blpscrud.controller.dto.response.CamundaTaskCompleteResponse;
import ru.gigasigma.blpscrud.controller.dto.response.CamundaTaskResponse;
import ru.gigasigma.blpscrud.service.CurrentUserService;

@Slf4j
@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
@Validated
@Tag(name = "Camunda Process")
public class CamundaProcessController {

    private static final String PURCHASE_PROCESS_KEY = "Process_Main";

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final CurrentUserService currentUserService;

    @PostMapping("/purchase/start")
    @Operation(summary = "Start Camunda ticket purchase process")
    public CamundaProcessStartResponse startPurchase(@RequestBody(required = false) @Valid StartPurchaseRequest request) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userId", currentUserService.getCurrentUserId());
        variables.put("processOwnerLogin", currentUserService.getCurrentLogin());
        if (request != null) {
            variables.put("flightId", request.flightId());
            variables.put("currency", request.currency());
            variables.put("seatNumber", request.seatNumber());
            variables.put("seatClass", request.seatClass().name());
            variables.put("hasBaggage", request.hasBaggage());
            variables.put("passengerName", request.passengerName());
            variables.put("passengerPassport", request.passengerPassport());
            variables.put("provider", request.provider());
        }
        variables.put("method", "internal");

        ProcessInstance instance = runtimeService.startProcessInstanceByKey(PURCHASE_PROCESS_KEY, variables);
        log.info("Camunda purchase process started. processInstanceId={}, userId={}, flightId={}",
                instance.getProcessInstanceId(), variables.get("userId"), variables.get("flightId"));
        return new CamundaProcessStartResponse(
                instance.getProcessInstanceId(),
                instance.getProcessDefinitionId(),
                instance.getBusinessKey(),
                runtimeService.getVariables(instance.getProcessInstanceId())
        );
    }

    @GetMapping("/tasks")
    @Operation(summary = "List active Camunda user tasks")
    public List<CamundaTaskResponse> tasks() {
        TaskQuery query = taskService.createTaskQuery().active();
        if (!currentUserService.isAdmin()) {
            query.taskAssignee(currentUserService.getCurrentLogin());
        }

        return query.orderByTaskCreateTime()
                .desc()
                .list()
                .stream()
                .map(CamundaTaskResponse::from)
                .toList();
    }

    @PostMapping("/tasks/{taskId}/complete")
    @Operation(summary = "Complete Camunda user task")
    public CamundaTaskCompleteResponse completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) CompleteCamundaTaskRequest request
    ) {
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .singleResult();
        if (task == null) {
            throw new EntityNotFoundException("Camunda task not found: " + taskId);
        }
        if (!currentUserService.isAdmin() && !currentUserService.getCurrentLogin().equals(task.getAssignee())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot complete another user's Camunda task");
        }

        String processInstanceId = task.getProcessInstanceId();
        Map<String, Object> variables = request == null || request.variables() == null
                ? Map.of()
                : request.variables();
        Set<String> allowed = switch (task.getTaskDefinitionKey()) {
            case "Task_User_Search" -> Set.of("from", "to", "date", "flightId");
            case "Task_User_Flight_Select" -> Set.of("hasSuitableFlight", "flightId");
            case "Task_User_Select" -> Set.of("method", "currency", "seatNumber", "seatClass",
                    "hasBaggage", "passengerName", "passengerPassport", "provider");
            default -> Set.of();
        };
        if (!allowed.containsAll(variables.keySet())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task contains unsupported input fields");
        }
        taskService.complete(taskId, variables);

        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        boolean processEnded = instance == null;
        Map<String, Object> currentVariables = processEnded ? Map.of() : runtimeService.getVariables(processInstanceId);
        log.info("Camunda user task completed. taskId={}, processInstanceId={}, processEnded={}",
                taskId, processInstanceId, processEnded);
        return new CamundaTaskCompleteResponse(processInstanceId, processEnded, currentVariables);
    }
}
