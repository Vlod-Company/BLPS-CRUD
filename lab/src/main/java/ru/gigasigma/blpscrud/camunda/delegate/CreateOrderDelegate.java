package ru.gigasigma.blpscrud.camunda.delegate;

import static ru.gigasigma.blpscrud.camunda.CamundaVariables.longValue;
import static ru.gigasigma.blpscrud.camunda.CamundaVariables.purchaseRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.gigasigma.blpscrud.controller.dto.response.PaymentRedirectResponse;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.enums.PaymentMethod;
import ru.gigasigma.blpscrud.service.OrderService;
import ru.gigasigma.blpscrud.service.internalPurchase.InternalPurchaseService;

@Slf4j
@Component("createOrderDelegate")
@RequiredArgsConstructor
public class CreateOrderDelegate implements JavaDelegate {

    private final OrderService orderService;
    private final InternalPurchaseService internalPurchaseService;

    @Override
    @Transactional
    public void execute(DelegateExecution execution) {
        Long userId = longValue(execution, "userId");
        Order order = orderService.createOrderWithTicket(purchaseRequest(execution), userId, PaymentMethod.INTERNAL, null);
        PaymentRedirectResponse redirect = internalPurchaseService.generatePaymentRedirectLink(order.getId());

        execution.setProcessBusinessKey(String.valueOf(order.getId()));
        execution.setVariable("orderId", order.getId());
        execution.setVariable("paymentMethod", order.getPaymentMethod().name());
        execution.setVariable("redirectUrl", redirect.redirectUrl());
        execution.setVariable("paymentSessionId", redirect.paymentSessionId());
        log.info("Camunda order created. processInstanceId={}, orderId={}", execution.getProcessInstanceId(), order.getId());
    }
}
