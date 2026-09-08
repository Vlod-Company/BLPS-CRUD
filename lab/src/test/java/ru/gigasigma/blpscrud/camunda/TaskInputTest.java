package ru.gigasigma.blpscrud.camunda;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import ru.gigasigma.blpscrud.controller.CamundaProcessController;
import ru.gigasigma.blpscrud.controller.dto.request.CompleteCamundaTaskRequest;
import ru.gigasigma.blpscrud.service.CurrentUserService;

class TaskInputTest {
    @Test
    void purchaseCanStartWithoutPrematureFlightOrPassengerDetails() {
        var runtime = mock(RuntimeService.class);
        var user = mock(CurrentUserService.class);
        var instance = mock(ProcessInstance.class);
        when(user.getCurrentUserId()).thenReturn(1L);
        when(user.getCurrentLogin()).thenReturn("alice");
        when(instance.getProcessInstanceId()).thenReturn("process");
        Map<String, Object> initial = Map.of("userId", 1L, "processOwnerLogin", "alice", "method", "internal");
        when(runtime.startProcessInstanceByKey("Process_Main", initial)).thenReturn(instance);
        var controller = new CamundaProcessController(runtime, mock(TaskService.class), user);

        controller.startPurchase(null);

        verify(runtime).startProcessInstanceByKey("Process_Main", initial);
    }

    @Test
    void completionCannotOverwriteOwnerSearchResultsOrPaymentState() {
        var tasks = mock(TaskService.class);
        var query = mock(TaskQuery.class);
        var task = mock(Task.class);
        var user = mock(CurrentUserService.class);
        when(tasks.createTaskQuery()).thenReturn(query);
        when(query.taskId("task")).thenReturn(query);
        when(query.singleResult()).thenReturn(task);
        when(task.getAssignee()).thenReturn("alice");
        when(user.getCurrentLogin()).thenReturn("alice");
        when(task.getTaskDefinitionKey()).thenReturn("Task_User_Flight_Select");
        var controller = new CamundaProcessController(mock(RuntimeService.class), tasks, user);

        for (String field : new String[] {"processOwnerLogin", "userId", "availableFlights", "orderId", "paymentStatus"}) {
            assertThatThrownBy(() -> controller.completeTask("task",
                    new CompleteCamundaTaskRequest(Map.of(field, "forged"))))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            error -> org.assertj.core.api.Assertions.assertThat(error.getStatusCode().value()).isEqualTo(400));
        }
        verify(tasks, never()).complete(anyString(), anyMap());
    }
}
