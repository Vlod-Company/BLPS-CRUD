package ru.gigasigma.blpscrud.camunda.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.security.XmlUserStore;
import ru.gigasigma.blpscrud.service.CurrentUserService;

@Component("initializePurchaseListener")
@RequiredArgsConstructor
public class InitializePurchaseListener implements ExecutionListener {

    private final IdentityService identityService;
    private final CurrentUserService currentUserService;
    private final XmlUserStore userStore;

    @Override
    public void notify(DelegateExecution execution) {
        var authentication = identityService.getCurrentAuthentication();
        String login = authentication == null
                ? currentUserService.getCurrentLogin() : authentication.getUserId();
        var account = userStore.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Application account not found: " + login));
        execution.setVariable("processOwnerLogin", account.login());
        execution.setVariable("userId", account.id());
        execution.setVariable("method", "internal");
    }
}
