package ru.gigasigma.blpscrud.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import ru.gigasigma.blpscrud.entity.Flight;
import ru.gigasigma.blpscrud.entity.Order;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.entity.Ticket;
import ru.gigasigma.blpscrud.service.TicketPdfService;

@Service
public class TicketPdfServiceImpl implements TicketPdfService {

    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float LEFT = 40f;
    private static final float RIGHT = PAGE_WIDTH - 40f;
    private static final float TOP = PAGE_HEIGHT - 40f;
    private static final float BOTTOM = 40f;
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.ENGLISH);

    @Override
    public byte[] generate(Ticket ticket) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Order order = ticket.getOrder();
            Flight flight = ticket.getFlight();
            List<Ticket> passengers = new ArrayList<>(order.getTickets());
            if (passengers.isEmpty()) {
                passengers.add(ticket);
            }
            passengers.sort(Comparator.comparing(Ticket::getId));

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            float y;
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                y = drawHeader(content, flight, order, ticket);
                y = drawFlightBlock(content, y, flight);
                y = drawFareAndPolicyBlock(content, y, order, passengers);
                y = drawImportantNotice(content, y);
                y = drawPassengerTableHeader(content, y - 8f);
                y = y - 28f;
            }

            int rowNumber = 1;
            for (Ticket passengerTicket : passengers) {
                if (y < BOTTOM + 40f) {
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    try (PDPageContentStream next = new PDPageContentStream(document, page)) {
                        y = TOP;
                        drawPassengerTableHeader(next, y);
                        y = y - 28f;
                        y = drawPassengerRow(next, y, rowNumber, passengerTicket);
                    }
                } else {
                    try (PDPageContentStream rowContent =
                                 new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)) {
                        y = drawPassengerRow(rowContent, y, rowNumber, passengerTicket);
                    }
                }
                rowNumber++;
            }

            try (PDPageContentStream footer =
                         new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)) {
                drawFooter(footer, ticket);
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate ticket PDF", e);
        }
    }

    private float drawHeader(PDPageContentStream content, Flight flight, Order order, Ticket ticket) throws IOException {
        content.setNonStrokingColor(17, 44, 79);
        content.addRect(LEFT, TOP - 70f, RIGHT - LEFT, 70f);
        content.fill();
        content.setNonStrokingColor(255, 255, 255);

        drawText(content, PDType1Font.HELVETICA_BOLD, 17f, LEFT + 12f, TOP - 27f,
                sanitize(flight.getAirline().getName()).toUpperCase(Locale.ENGLISH));
        drawText(content, PDType1Font.HELVETICA, 10f, LEFT + 12f, TOP - 43f,
                "ELECTRONIC TICKET ITINERARY RECEIPT");
        drawText(content, PDType1Font.HELVETICA, 9f, LEFT + 12f, TOP - 58f,
                "Order #" + order.getId() + " | Ticket #" + ticket.getId() + " | Issued " + DATE_TIME_FORMAT.format(order.getCreatedAt()));

        content.setNonStrokingColor(0, 0, 0);
        return TOP - 86f;
    }

    private float drawFlightBlock(PDPageContentStream content, float y, Flight flight) throws IOException {
        float boxHeight = 110f;
        drawBox(content, LEFT, y - boxHeight, RIGHT - LEFT, boxHeight);
        drawText(content, PDType1Font.HELVETICA_BOLD, 12f, LEFT + 10f, y - 18f, "FLIGHT INFORMATION");
        drawText(content, PDType1Font.HELVETICA_BOLD, 18f, LEFT + 10f, y - 46f,
                sanitize(flight.getDepartureAirport()) + "  ->  " + sanitize(flight.getArrivalAirport()));
        drawText(content, PDType1Font.HELVETICA, 11f, LEFT + 10f, y - 64f,
                "Flight: " + sanitize(flight.getFlightNumber()) + "  |  Aircraft: " + sanitize(flight.getAircraftType()));
        drawText(content, PDType1Font.HELVETICA, 11f, LEFT + 10f, y - 82f,
                "Departure: " + DATE_TIME_FORMAT.format(flight.getDepartureTime()));
        drawText(content, PDType1Font.HELVETICA, 11f, LEFT + 260f, y - 82f,
                "Arrival: " + DATE_TIME_FORMAT.format(flight.getArrivalTime()));
        return y - boxHeight - 14f;
    }

    private float drawFareAndPolicyBlock(PDPageContentStream content, float y, Order order, List<Ticket> passengers) throws IOException {
        float boxHeight = 86f;
        drawBox(content, LEFT, y - boxHeight, RIGHT - LEFT, boxHeight);
        drawText(content, PDType1Font.HELVETICA_BOLD, 12f, LEFT + 10f, y - 18f, "FARE AND BAGGAGE");
        drawText(content, PDType1Font.HELVETICA, 11f, LEFT + 10f, y - 37f,
                "Total paid: " + order.getTotalPrice() + " " + sanitize(order.getCurrency()));
        drawText(content, PDType1Font.HELVETICA, 11f, LEFT + 10f, y - 53f,
                "Carry-on: ECONOMY 1x8kg, BUSINESS 2x8kg");
        drawText(content, PDType1Font.HELVETICA, 11f, LEFT + 10f, y - 69f,
                "Checked baggage: " + baggageSummary(passengers));
        return y - boxHeight - 14f;
    }

    private float drawImportantNotice(PDPageContentStream content, float y) throws IOException {
        float boxHeight = 86f;
        drawBox(content, LEFT, y - boxHeight, RIGHT - LEFT, boxHeight);
        drawText(content, PDType1Font.HELVETICA_BOLD, 12f, LEFT + 10f, y - 18f, "IMPORTANT TRAVEL NOTES");
        drawText(content, PDType1Font.HELVETICA, 10f, LEFT + 10f, y - 35f,
                "1) Please arrive at the airport at least 3 hours before departure.");
        drawText(content, PDType1Font.HELVETICA, 10f, LEFT + 10f, y - 50f,
                "2) Online check-in and visa/transit requirements are passenger responsibility.");
        drawText(content, PDType1Font.HELVETICA, 10f, LEFT + 10f, y - 65f,
                "3) Boarding gate closes 20 minutes before departure unless stated otherwise.");
        return y - boxHeight - 6f;
    }

    private float drawPassengerTableHeader(PDPageContentStream content, float y) throws IOException {
        drawText(content, PDType1Font.HELVETICA_BOLD, 12f, LEFT, y, "PASSENGERS");
        float headerY = y - 16f;
        drawBox(content, LEFT, headerY - 18f, RIGHT - LEFT, 18f);
        drawText(content, PDType1Font.HELVETICA_BOLD, 9.5f, LEFT + 4f, headerY - 12f, "#");
        drawText(content, PDType1Font.HELVETICA_BOLD, 9.5f, LEFT + 24f, headerY - 12f, "Passenger");
        drawText(content, PDType1Font.HELVETICA_BOLD, 9.5f, LEFT + 220f, headerY - 12f, "Passport");
        drawText(content, PDType1Font.HELVETICA_BOLD, 9.5f, LEFT + 330f, headerY - 12f, "Seat");
        drawText(content, PDType1Font.HELVETICA_BOLD, 9.5f, LEFT + 385f, headerY - 12f, "Class");
        drawText(content, PDType1Font.HELVETICA_BOLD, 9.5f, LEFT + 465f, headerY - 12f, "Baggage");
        return headerY - 4f;
    }

    private float drawPassengerRow(PDPageContentStream content, float y, int rowNumber, Ticket t) throws IOException {
        float rowHeight = 18f;
        drawBox(content, LEFT, y - rowHeight, RIGHT - LEFT, rowHeight);
        drawText(content, PDType1Font.HELVETICA, 9f, LEFT + 4f, y - 12f, String.valueOf(rowNumber));
        drawText(content, PDType1Font.HELVETICA, 9f, LEFT + 24f, y - 12f, crop(sanitize(t.getPassengerName()), 30));
        drawText(content, PDType1Font.HELVETICA, 9f, LEFT + 220f, y - 12f, crop(sanitize(t.getPassengerPassport()), 18));
        drawText(content, PDType1Font.HELVETICA, 9f, LEFT + 330f, y - 12f, crop(sanitize(t.getSeatNumber()), 6));
        drawText(content, PDType1Font.HELVETICA, 9f, LEFT + 385f, y - 12f, sanitize(t.getSeatClass().name()));
        drawText(content, PDType1Font.HELVETICA, 9f, LEFT + 465f, y - 12f,
                Boolean.TRUE.equals(t.getHasBaggage()) ? "Included" : "No");
        return y - rowHeight;
    }

    private void drawFooter(PDPageContentStream content, Ticket ticket) throws IOException {
        String code = "ETKT-" + ticket.getOrder().getId() + "-" + ticket.getId() + "-" + ticket.getFlight().getFlightNumber();
        drawText(content, PDType1Font.HELVETICA, 8.5f, LEFT, BOTTOM + 8f, "Booking code: " + code);
        drawText(content, PDType1Font.HELVETICA, 8.5f, RIGHT - 180f, BOTTOM + 8f, "Generated by BLPS-CRUD demo airline module");
    }

    private String baggageSummary(List<Ticket> tickets) {
        long withBag = tickets.stream().filter(t -> Boolean.TRUE.equals(t.getHasBaggage())).count();
        if (withBag == 0) {
            return "No checked baggage included.";
        }
        if (withBag == tickets.size()) {
            return "Checked baggage included for all passengers.";
        }
        return "Checked baggage included for " + withBag + " of " + tickets.size() + " passengers.";
    }

    private void drawBox(PDPageContentStream content, float x, float y, float width, float height) throws IOException {
        content.setStrokingColor(90, 90, 90);
        content.addRect(x, y, width, height);
        content.stroke();
    }

    private void drawText(PDPageContentStream content, PDFont font, float size, float x, float y, String text) throws IOException {
        content.setFont(font, size);
        content.beginText();
        content.newLineAtOffset(x, y);
        content.setRenderingMode(RenderingMode.FILL);
        content.showText(sanitize(text));
        content.endText();
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\n', ' ').replace('\r', ' ').trim();
    }

    private String crop(String value, int maxLen) {
        if (value.length() <= maxLen) {
            return value;
        }
        return value.substring(0, maxLen - 3) + "...";
    }
}
