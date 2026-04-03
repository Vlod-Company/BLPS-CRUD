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
import ru.gigasigma.blpscrud.controller.dto.request.PaymentCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.request.StartPurchaseRequest;
import ru.gigasigma.blpscrud.controller.dto.response.PaymentRedirectResponse;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;
import ru.gigasigma.blpscrud.service.internalPurchase.InternalPurchaseService;

@RestController
@RequestMapping("/api/internal-purchases")
@RequiredArgsConstructor
@Validated
@Tag(name = "Internal Purchases")
public class InternalPurchaseController {

    private final InternalPurchaseService internalPurchaseService;

    @PostMapping
    @Operation(summary = "Start internal purchase", description = "Creates an internal order for the authenticated user and redirects to the payment page.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirect to payment page"),
            @ApiResponse(responseCode = "400", description = "Invalid purchase payload", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Flight not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> create(@RequestBody @Valid StartPurchaseRequest request) {
        PaymentRedirectResponse redirect = internalPurchaseService.startInternalPurchase(request);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirect.redirectUrl()))
                .build();
    }

    @PostMapping("/callback")
    @Operation(summary = "Handle internal payment callback", description = "Applies the result of an internal payment callback to the order workflow.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Callback processed", content = @Content(schema = @Schema(implementation = WorkflowResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid callback payload", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public WorkflowResult callback(@RequestBody @Valid PaymentCallbackRequest request) {
        return internalPurchaseService.handlePaymentCallback(request);
    }
}
