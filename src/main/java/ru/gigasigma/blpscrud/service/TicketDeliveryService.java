package ru.gigasigma.blpscrud.service;

import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.entity.Ticket;

public interface TicketDeliveryService {
    void sendTicket(Order order, Ticket ticket);
}
