package ru.gigasigma.blpscrud.service.ticketDelivery.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.entity.Ticket;
import ru.gigasigma.blpscrud.service.ticketDelivery.TicketDeliveryService;

@Service
@Slf4j
public class StubTicketDeliveryService implements TicketDeliveryService {

    @Override
    public void sendTicket(Order order, Ticket ticket) {
        log.info("Ticket sent to user. orderId={}, ticketId={}", order.getId(), ticket.getId());
    }
}
