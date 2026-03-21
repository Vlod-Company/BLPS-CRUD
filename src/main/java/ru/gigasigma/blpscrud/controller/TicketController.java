package ru.gigasigma.blpscrud.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gigasigma.blpscrud.controller.dto.TicketResponse;
import ru.gigasigma.blpscrud.entity.Ticket;
import ru.gigasigma.blpscrud.repository.TicketRepository;
import ru.gigasigma.blpscrud.service.TicketPdfService;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Validated
public class TicketController {

    private final TicketRepository ticketRepository;
    private final TicketPdfService ticketPdfService;

    @GetMapping("/{id}")
    public TicketResponse getById(@PathVariable @Positive(message = "id должен быть положительным числом") Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Ticket not found: " + id));
        return TicketResponse.fromEntity(ticket);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable @Positive(message = "id должен быть положительным числом") Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Ticket not found: " + id));
        byte[] pdf = ticketPdfService.generate(ticket);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("ticket-" + id + ".pdf").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}
