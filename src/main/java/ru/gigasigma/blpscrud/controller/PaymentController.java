package ru.gigasigma.blpscrud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gigasigma.blpscrud.controller.dto.PaymentCallbackRequest;
import ru.gigasigma.blpscrud.service.InternalPurchaseService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
@Tag(name = "Payments")
public class PaymentController {

    private final InternalPurchaseService purchaseService;

    @PostMapping("/callback")
    @Operation(summary = "Handle payment callback", description = "Applies the result of an internal payment provider callback to the order workflow.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Callback processed", content = @Content(schema = @Schema(implementation = WorkflowResult.class))),
            @ApiResponse(responseCode = "400", description = "Invalid callback payload", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public WorkflowResult callback(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Payment callback payload",
                    content = @Content(schema = @Schema(implementation = PaymentCallbackRequest.class))
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid PaymentCallbackRequest request
    ) {
        return purchaseService.handlePaymentCallback(request);
    }
}
