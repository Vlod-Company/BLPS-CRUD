package ru.gigasigma.blpscrud.service.impl;

import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.entity.Ticket;
import ru.gigasigma.blpscrud.service.AirlineBookingService;

@Service
public class StubAirlineBookingService implements AirlineBookingService {

    @Override
    public void issueTicket(Order order, Ticket ticket) {
        if (ticket.getFlight().getAvailableSeats() < 0) {
            throw new IllegalStateException("External airline booking rejected the ticket");
        }
    }
}
