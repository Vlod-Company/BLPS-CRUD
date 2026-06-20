package ru.gigasigma.blpscrud.camunda.delegate;

import static ru.gigasigma.blpscrud.camunda.CamundaVariables.booleanValue;
import static ru.gigasigma.blpscrud.camunda.CamundaVariables.decimalValue;
import static ru.gigasigma.blpscrud.camunda.CamundaVariables.longValue;
import static ru.gigasigma.blpscrud.camunda.CamundaVariables.stringValue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.enums.OrderStatus;
import ru.gigasigma.blpscrud.service.OrderService;

@Slf4j
@Component("processPaymentDelegate")
@RequiredArgsConstructor
public class ProcessPaymentDelegate implements JavaDelegate {

    private final OrderService orderService;

    @Override
    public void execute(DelegateExecution execution) {
        Long orderId = longValue(execution, "orderId");
        Boolean paymentSuccess = booleanValue(execution, "paymentSuccess");
        Order order = orderService.getOrder(orderId);

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new BpmnError("PAYMENT_FAILED", "Canceled order cannot be paid");
        }
        if (!Boolean.TRUE.equals(paymentSuccess)) {
            String reason = stringValue(execution, "failureReason");
            throw new BpmnError("PAYMENT_FAILED", reason == null ? "Payment failed" : reason);
        }
        if (decimalValue(execution, "paidAmount") != null && decimalValue(execution, "paidAmount").compareTo(order.getTotalPrice()) < 0) {
            throw new BpmnError("PAYMENT_FAILED", "Paid amount is less than order total");
        }

        execution.setVariable("paymentProcessed", true);
        log.info("Camunda payment processed. orderId={}, externalPaymentId={}", orderId, stringValue(execution, "externalPaymentId"));
    }
}
