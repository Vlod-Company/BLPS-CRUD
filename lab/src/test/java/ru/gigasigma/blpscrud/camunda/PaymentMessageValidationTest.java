package ru.gigasigma.blpscrud.camunda;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import ru.gigasigma.blpscrud.listener.PaymentCallbackListener;
import ru.gigasigma.blpscrud.service.PaymentCallbackProcessingService;

class PaymentMessageValidationTest {
    @Test
    void missingAmountCannotReachPurchaseProcessing() throws Exception {
        var processing = mock(PaymentCallbackProcessingService.class);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var listener = new PaymentCallbackListener(processing, new ObjectMapper(), factory.getValidator());
            assertThatThrownBy(() -> listener.handleCallback(
                    "{\"orderId\":1,\"success\":true,\"externalPaymentId\":\"payment\"}"))
                    .isInstanceOf(ConstraintViolationException.class).hasMessageContaining("paidAmount");
            assertThatThrownBy(() -> listener.handleCallback("null")).isInstanceOf(IllegalArgumentException.class);
            verifyNoInteractions(processing);

            listener.handleCallback("{\"orderId\":1,\"success\":true,\"externalPaymentId\":\"payment\",\"paidAmount\":10}");
            verify(processing).handleCallback(argThat(request -> request.orderId().equals(1L)
                    && request.paidAmount().intValueExact() == 10));
        }
    }
}
