package ru.gigasigma.blpscrud.camunda;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import ru.gigasigma.blpscrud.camunda.delegate.ExternalPurchaseDelegate;
import ru.gigasigma.blpscrud.controller.dto.request.ExternalBookingCallbackRequest;
import ru.gigasigma.blpscrud.enums.OrderStatus;
import ru.gigasigma.blpscrud.enums.SeatClass;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;
import ru.gigasigma.blpscrud.service.externalAirlineLogic.ExternalPurchaseService;

class ExternalPurchaseDelegateTest {
    @Test
    void persistsMatchingCallbackAndRejectsDifferentOwner() throws Exception {
        var mapper = new ObjectMapper();
        var service = mock(ExternalPurchaseService.class);
        var execution = mock(DelegateExecution.class);
        var request = new ExternalBookingCallbackRequest(1L, 2L, "RUB", "12A", SeatClass.ECONOMY,
                false, "Ivan Ivanov", "1234 567890", "ivan@example.com", "+79991234567",
                "payment", "booking", "ticket", BigDecimal.TEN);
        when(execution.getVariable("externalBookingCallback")).thenReturn(mapper.writeValueAsString(request));
        when(execution.getVariable("userId")).thenReturn(1L);
        when(execution.getVariable("flightId")).thenReturn(2L);
        when(service.completeExternalBookingForProcess(request)).thenReturn(
                new WorkflowResult(3L, OrderStatus.PAID, BigDecimal.TEN, "RUB", "completed", null));
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var delegate = new ExternalPurchaseDelegate(service, mapper, factory.getValidator());
            delegate.execute(execution);
            verify(service).completeExternalBookingForProcess(request);
            verify(service, never()).completeExternalBooking(any());
            verify(execution).setVariable("orderId", 3L);
            verify(execution).setProcessBusinessKey("3");

            clearInvocations(service);
            when(execution.getVariable("userId")).thenReturn(99L);
            assertThatThrownBy(() -> delegate.execute(execution))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("owner or flight");
            verifyNoInteractions(service);
        }
    }
}
