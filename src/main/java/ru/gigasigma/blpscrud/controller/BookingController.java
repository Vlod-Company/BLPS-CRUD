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
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gigasigma.blpscrud.controller.dto.ExternalBookingCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.ExternalRedirectRequest;
import ru.gigasigma.blpscrud.controller.dto.RedirectResponse;
import ru.gigasigma.blpscrud.service.ExternalPurchaseService;
import ru.gigasigma.blpscrud.service.FlightService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;
import ru.gigasigma.blpscrud.util.ExternalPurchaseServiceFactory;

@RestController
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequestMapping("/api/booking")
@RequiredArgsConstructor
@Validated
@Tag(name = "Booking")
public class BookingController {

    private final ExternalPurchaseServiceFactory externalPurchaseServiceFactory;
    private final FlightService flightService;

    @PostMapping("/redirect")
    @Operation(summary = "Generate external booking redirect", description = "Creates an external booking session and responds with HTTP 302 to the provider page.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirect to provider booking page"),
            @ApiResponse(responseCode = "400", description = "Invalid redirect payload", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Flight or provider not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> generateRedirect(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "External redirect payload",
                    content = @Content(schema = @Schema(implementation = ExternalRedirectRequest.class))
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid ExternalRedirectRequest request
    ) {
        ExternalPurchaseService service = externalPurchaseServiceFactory.getService(request.provider());
        RedirectResponse redirect = service.generateRedirectLink(request);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(redirect.redirectUrl()))
                .build();
    }

    @PostMapping("/external/callback")
    @Operation(summary = "Handle external booking callback", description = "Finalizes an externally created booking and persists the resulting order state.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Callback processed", content = @Content(schema = @Schema(implementation = WorkflowResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid callback payload", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Flight or provider not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public WorkflowResult externalCallback(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "External provider booking callback payload",
                    content = @Content(schema = @Schema(implementation = ExternalBookingCallbackRequest.class))
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid ExternalBookingCallbackRequest request
    ) {
        String iata = flightService.getById(request.flightId()).getAirline().getIataCode();
        ExternalPurchaseService service = externalPurchaseServiceFactory.getService(iata);
        return service.completeExternalBooking(request);
    }
}
