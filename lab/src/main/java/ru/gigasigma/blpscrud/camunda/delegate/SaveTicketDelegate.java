package ru.gigasigma.blpscrud.camunda.delegate;

import static ru.gigasigma.blpscrud.camunda.CamundaVariables.longValue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.service.OrderService;

@Slf4j
@Component("saveTicketDelegate")
@RequiredArgsConstructor
public class SaveTicketDelegate implements JavaDelegate {

    private final OrderService orderService;

    @Override
    public void execute(DelegateExecution execution) {
        Long orderId = longValue(execution, "orderId");
        if (orderId == null) {
            throw new IllegalStateException("Cannot confirm saved ticket without an order");
        }
        var ticket = orderService.getOrderTicket(orderId);
        if (ticket == null || ticket.getId() == null) {
            throw new IllegalStateException("Order has no persisted ticket: " + orderId);
        }
        execution.setVariable("ticketId", ticket.getId());
        execution.setVariable("ticketSaved", true);
        log.info("Camunda save ticket step completed. orderId={}", orderId);
    }
}
