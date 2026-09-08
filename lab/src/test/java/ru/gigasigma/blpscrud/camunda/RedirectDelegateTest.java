package ru.gigasigma.blpscrud.camunda;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import ru.gigasigma.blpscrud.camunda.delegate.RedirectDelegate;
import ru.gigasigma.blpscrud.controller.dto.request.ExternalRedirectRequest;
import ru.gigasigma.blpscrud.controller.dto.response.RedirectResponse;
import ru.gigasigma.blpscrud.service.externalAirlineLogic.ExternalPurchaseService;

class RedirectDelegateTest {
    @Test
    void generatesLinkForProcessOwnerAndPersistsSession() {
        var service = mock(ExternalPurchaseService.class);
        var execution = mock(DelegateExecution.class);
        when(execution.getVariable("userId")).thenReturn(2L);
        when(execution.getVariable("flightId")).thenReturn(17L);
        when(execution.getVariable("currency")).thenReturn("RUB");
        when(execution.getVariable("provider")).thenReturn("airline");
        var request = new ExternalRedirectRequest(17L, "RUB", "airline");
        when(service.generateRedirectLink(request, 2L))
                .thenReturn(new RedirectResponse("https://airline.example/booking", "session-17"));

        new RedirectDelegate(service).execute(execution);

        verify(service).generateRedirectLink(request, 2L);
        verify(execution).setVariable("redirectUrl", "https://airline.example/booking");
        verify(execution).setVariable("externalSessionId", "session-17");
        verify(execution).setVariable("redirectPrepared", true);
    }

    @Test
    void invalidProviderResponseDoesNotMarkRedirectPrepared() {
        var service = mock(ExternalPurchaseService.class);
        var execution = mock(DelegateExecution.class);
        when(service.generateRedirectLink(any(), any())).thenReturn(new RedirectResponse("", "session"));

        assertThatThrownBy(() -> new RedirectDelegate(service).execute(execution))
                .isInstanceOf(IllegalStateException.class);

        verify(execution, never()).setVariable(eq("redirectPrepared"), any());
    }
}
