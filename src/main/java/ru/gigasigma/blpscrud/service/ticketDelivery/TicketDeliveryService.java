package ru.gigasigma.blpscrud.service.ticketDelivery;

import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.entity.Ticket;

public interface TicketDeliveryService {
    void sendTicket(Order order, Ticket ticket);
}
