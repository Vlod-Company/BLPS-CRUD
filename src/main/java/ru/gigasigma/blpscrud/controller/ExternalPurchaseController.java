package ru.gigasigma.blpscrud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gigasigma.blpscrud.controller.dto.request.ExternalBookingCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.request.ExternalRedirectRequest;
import ru.gigasigma.blpscrud.controller.dto.response.RedirectResponse;
import ru.gigasigma.blpscrud.service.FlightService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;
import ru.gigasigma.blpscrud.service.externalAirlineLogic.ExternalPurchaseService;
import ru.gigasigma.blpscrud.util.ExternalPurchaseServiceFactory;

@RestController
@RequestMapping("/api/external-purchases")
@RequiredArgsConstructor
@Validated
@Tag(name = "External Purchases")
public class ExternalPurchaseController {

    private final ExternalPurchaseServiceFactory externalPurchaseServiceFactory;
    private final FlightService flightService;

    @PostMapping("/redirect")
    @Operation(summary = "Generate external purchase redirect", description = "Creates an external booking session for the authenticated user and redirects to the provider page.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirect to provider page"),
            @ApiResponse(responseCode = "400", description = "Invalid redirect payload", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Flight or provider not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> generateRedirect(@RequestBody @Valid ExternalRedirectRequest request) {
        ExternalPurchaseService service = externalPurchaseServiceFactory.getService(request.provider());
        RedirectResponse redirect = service.generateRedirectLink(request);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirect.redirectUrl()))
                .build();
    }

    @PostMapping("/callback")
    @Operation(summary = "Handle external purchase callback", description = "Finalizes an externally created booking and persists the resulting order state.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Callback processed", content = @Content(schema = @Schema(implementation = WorkflowResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid callback payload", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Flight or provider not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public WorkflowResult callback(@RequestBody @Valid ExternalBookingCallbackRequest request) {
        String iata = flightService.getById(request.flightId()).getAirline().getIataCode();
        ExternalPurchaseService service = externalPurchaseServiceFactory.getService(iata);
        return service.completeExternalBooking(request);
    }
}
