package ru.gigasigma.blpscrud.camunda.delegate;

import static ru.gigasigma.blpscrud.camunda.CamundaVariables.longValue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.entity.Ticket;
import ru.gigasigma.blpscrud.service.OrderService;
import ru.gigasigma.blpscrud.service.ticketDelivery.TicketDeliveryService;

@Slf4j
@Component("emailDelegate")
@RequiredArgsConstructor
public class EmailDelegate implements JavaDelegate {

    private final OrderService orderService;
    private final TicketDeliveryService ticketDeliveryService;

    @Override
    public void execute(DelegateExecution execution) {
        Long orderId = longValue(execution, "orderId");
        Order order = orderService.getOrder(orderId);
        Ticket ticket = orderService.getOrderTicket(orderId);
        ticketDeliveryService.sendTicket(order, ticket);
        execution.setVariable("ticketDelivered", true);
        log.info("Camunda ticket delivery completed. orderId={}, ticketId={}", orderId, ticket.getId());
    }
}
