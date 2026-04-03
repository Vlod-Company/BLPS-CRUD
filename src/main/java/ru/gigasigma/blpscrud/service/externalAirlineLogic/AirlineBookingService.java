package ru.gigasigma.blpscrud.service.externalAirlineLogic;

import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.entity.Ticket;

public interface AirlineBookingService {
    void issueTicket(Order order, Ticket ticket);
}
