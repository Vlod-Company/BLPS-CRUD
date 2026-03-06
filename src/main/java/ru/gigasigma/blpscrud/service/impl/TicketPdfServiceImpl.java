package ru.gigasigma.blpscrud.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.entity.Ticket;
import ru.gigasigma.blpscrud.service.TicketPdfService;

@Service
public class TicketPdfServiceImpl implements TicketPdfService {

    @Override
    public byte[] generate(Ticket ticket) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.setFont(PDType1Font.HELVETICA_BOLD, 16);
                content.beginText();
                content.newLineAtOffset(60, 780);
                content.showText("Flight Ticket");
                content.endText();

                content.setFont(PDType1Font.HELVETICA, 12);
                float y = 740;
                y = writeLine(content, y, "Ticket ID: " + ticket.getId());
                y = writeLine(content, y, "Order ID: " + ticket.getOrder().getId());
                y = writeLine(content, y, "Passenger: " + ticket.getPassengerName());
                y = writeLine(content, y, "Passport: " + ticket.getPassengerPassport());
                y = writeLine(content, y, "Flight: " + ticket.getFlight().getFlightNumber());
                y = writeLine(content, y, "Route: " + ticket.getFlight().getDepartureAirport() + " -> " + ticket.getFlight().getArrivalAirport());
                y = writeLine(content, y, "Seat: " + ticket.getSeatNumber());
                y = writeLine(content, y, "Class: " + ticket.getSeatClass().name());
                writeLine(content, y, "Baggage: " + (Boolean.TRUE.equals(ticket.getHasBaggage()) ? "YES" : "NO"));
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate ticket PDF", e);
        }
    }

    private float writeLine(PDPageContentStream content, float y, String text) throws IOException {
        content.beginText();
        content.newLineAtOffset(60, y);
        content.showText(text);
        content.endText();
        return y - 22;
    }
}
