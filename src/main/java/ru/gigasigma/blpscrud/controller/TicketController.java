package ru.gigasigma.blpscrud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Tickets")
public class TicketController {

    private final TicketRepository ticketRepository;
    private final TicketPdfService ticketPdfService;

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by id", description = "Returns ticket details for an issued booking.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket found", content = @Content(schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid identifier", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public TicketResponse getById(
            @Parameter(description = "Ticket identifier", example = "901", required = true)
            @PathVariable @Positive(message = "id must be a positive number") Long id
    ) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Ticket not found: " + id));
        return TicketResponse.fromEntity(ticket);
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Download ticket PDF", description = "Returns the ticket as a generated PDF file.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF generated", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "400", description = "Invalid identifier", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public ResponseEntity<byte[]> downloadPdf(
            @Parameter(description = "Ticket identifier", example = "901", required = true)
            @PathVariable @Positive(message = "id must be a positive number") Long id
    ) {
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
