package ru.gigasigma.blpscrud.camunda;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.junit.jupiter.api.Test;
import ru.gigasigma.blpscrud.controller.dto.request.PaymentCallbackRequest;
import ru.gigasigma.blpscrud.service.PaymentCallbackProcessingService;
import ru.gigasigma.blpscrud.service.internalPurchase.InternalPurchaseService;

class PaymentCallbackRoutingTest {
    @Test
    void earlyCallbackDoesNotBypassActiveProcess() {
        var runtime = mock(RuntimeService.class);
        var legacy = mock(InternalPurchaseService.class);
        var correlation = mock(MessageCorrelationBuilder.class, RETURNS_SELF);
        var query = mock(ProcessInstanceQuery.class, RETURNS_SELF);
        var request = mock(PaymentCallbackRequest.class);
        when(request.orderId()).thenReturn(1L);
        when(runtime.createMessageCorrelation("PaymentReceived")).thenReturn(correlation);
        when(runtime.createProcessInstanceQuery()).thenReturn(query);
        when(query.count()).thenReturn(1L);
        doThrow(new MismatchingMessageCorrelationException("PaymentReceived")).when(correlation).correlate();

        var service = new PaymentCallbackProcessingService(legacy, runtime);
        assertThatThrownBy(() -> service.handleCallback(request)).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(legacy);

        when(query.count()).thenReturn(0L);
        service.handleCallback(request);
        verify(legacy).handlePaymentCallback(request);
    }
}
