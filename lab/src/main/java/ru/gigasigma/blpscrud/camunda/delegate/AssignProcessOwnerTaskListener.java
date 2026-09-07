package ru.gigasigma.blpscrud.camunda.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

@Component("assignProcessOwnerTaskListener")
@Slf4j
public class AssignProcessOwnerTaskListener implements TaskListener {

    private static final String PROCESS_OWNER_LOGIN = "processOwnerLogin";

    @Override
    public void notify(DelegateTask task) {
        Object ownerLogin = task.getVariable(PROCESS_OWNER_LOGIN);
        if (!(ownerLogin instanceof String login) || login.isBlank()) {
            log.warn("Cannot assign Camunda task without process owner. taskId={}, taskDefinitionKey={}",
                    task.getId(), task.getTaskDefinitionKey());
            return;
        }

        task.setOwner(login);
        task.setAssignee(login);
        log.debug("Assigned Camunda task to process owner. taskDefinitionKey={}, assignee={}",
                task.getTaskDefinitionKey(), login);
    }
}
