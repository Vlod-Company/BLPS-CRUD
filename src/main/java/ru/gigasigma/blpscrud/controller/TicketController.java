package ru.gigasigma.blpscrud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import java.util.List;
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
import ru.gigasigma.blpscrud.controller.dto.response.TicketResponse;
import ru.gigasigma.blpscrud.entity.Ticket;
import ru.gigasigma.blpscrud.service.OrderService;
import ru.gigasigma.blpscrud.service.TicketPdfService;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Validated
@Tag(name = "Tickets")
public class TicketController {

    private final TicketPdfService ticketPdfService;
    private final OrderService orderService;

    @GetMapping("/my")
    @Operation(summary = "Get my tickets", description = "Returns all tickets belonging to the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets loaded", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TicketResponse.class))))
    })
    public List<TicketResponse> myTickets() {
        return orderService.findCurrentUserTickets();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by id", description = "Returns ticket details for the owner or for ROLE_ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket found", content = @Content(schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid identifier", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public TicketResponse getById(
            @Parameter(description = "Ticket identifier", example = "901", required = true)
            @PathVariable @Positive(message = "id must be a positive number") Long id
    ) {
        Ticket ticket = orderService.getAccessibleTicket(id);
        return TicketResponse.fromEntity(ticket);
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Download ticket PDF", description = "Returns the ticket as a generated PDF file for the owner or for ROLE_ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF generated", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "400", description = "Invalid identifier", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public ResponseEntity<byte[]> downloadPdf(
            @Parameter(description = "Ticket identifier", example = "901", required = true)
            @PathVariable @Positive(message = "id must be a positive number") Long id
    ) {
        Ticket ticket = orderService.getAccessibleTicket(id);
        byte[] pdf = ticketPdfService.generate(ticket);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment().filename("ticket-" + id + ".pdf").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }
}
