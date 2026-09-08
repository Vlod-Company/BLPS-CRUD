package ru.gigasigma.blpscrud.camunda;

import java.util.ArrayList;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.form.validator.FormFieldValidationException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.springframework.stereotype.Component;

@Component
public class PurchaseValidationPlugin extends AbstractProcessEnginePlugin {
    @Override
    public void preInit(ProcessEngineConfigurationImpl configuration) {
        var interceptors = configuration.getCustomPreCommandInterceptorsTxRequired();
        if (interceptors == null) {
            interceptors = new ArrayList<>();
            configuration.setCustomPreCommandInterceptorsTxRequired(interceptors);
        }
        interceptors.add(new ValidationInterceptor());
    }

    private static class ValidationInterceptor extends CommandInterceptor {
        @Override
        public <T> T execute(Command<T> command) {
            try {
                return next.execute(command);
            } catch (ProcessEngineException exception) {
                for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                    if (cause instanceof PurchaseFormValidationException validation) {
                        throw new FormFieldValidationException("purchaseForm", validation.getMessage(), validation);
                    }
                }
                throw exception;
            }
        }
    }
}
