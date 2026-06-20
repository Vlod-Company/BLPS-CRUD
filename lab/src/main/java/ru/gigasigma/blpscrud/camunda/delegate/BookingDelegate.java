package ru.gigasigma.blpscrud.camunda.delegate;

import static ru.gigasigma.blpscrud.camunda.CamundaVariables.longValue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.entity.Ticket;
import ru.gigasigma.blpscrud.enums.OrderStatus;
import ru.gigasigma.blpscrud.repository.OrderRepository;
import ru.gigasigma.blpscrud.service.OrderService;
import ru.gigasigma.blpscrud.service.externalAirlineLogic.AirlineBookingService;

@Slf4j
@Component("bookingDelegate")
@RequiredArgsConstructor
public class BookingDelegate implements JavaDelegate {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final AirlineBookingService airlineBookingService;

    @Override
    @Transactional
    public void execute(DelegateExecution execution) {
        Long orderId = longValue(execution, "orderId");
        Order order = orderService.getOrder(orderId);
        if (order.getStatus() == OrderStatus.PAID) {
            log.info("Order already paid and issued. orderId={}", orderId);
            return;
        }

        orderService.assertPending(order);
        Ticket ticket = orderService.getOrderTicket(orderId);
        airlineBookingService.issueTicket(order, ticket);
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        execution.setVariable("ticketId", ticket.getId());
        execution.setVariable("orderStatus", OrderStatus.PAID.name());
        log.info("Camunda booking completed. orderId={}, ticketId={}", orderId, ticket.getId());
    }
}
