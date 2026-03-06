package ru.gigasigma.blpscrud.service;

import ru.gigasigma.blpscrud.entity.Ticket;

public interface TicketPdfService {

    byte[] generate(Ticket ticket);
}
