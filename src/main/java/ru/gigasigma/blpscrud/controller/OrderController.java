package ru.gigasigma.blpscrud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.gigasigma.blpscrud.controller.dto.response.OrderResponse;
import ru.gigasigma.blpscrud.controller.dto.response.PaymentRedirectResponse;
import ru.gigasigma.blpscrud.controller.dto.request.StartPurchaseRequest;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.service.internalPurchase.InternalPurchaseService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;
import ru.gigasigma.blpscrud.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Orders")
public class OrderController {

    private final InternalPurchaseService internalPurchaseService;
    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create order", description = "Creates an internal order for the authenticated user and responds with a redirect to the payment page.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirect to payment page"),
            @ApiResponse(responseCode = "400", description = "Invalid order payload", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Flight not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Order creation payload",
                    content = @Content(schema = @Schema(implementation = StartPurchaseRequest.class))
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid StartPurchaseRequest request
    ) {
        PaymentRedirectResponse redirect = internalPurchaseService.startInternalPurchase(request);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(redirect.redirectUrl()))
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by id", description = "Returns a single order when it belongs to the authenticated user or the caller has ROLE_ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found", content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid identifier", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public OrderResponse getById(
            @Parameter(description = "Order identifier", example = "501", required = true)
            @PathVariable @Positive(message = "id must be a positive number") Long id
    ) {
        Order order = orderService.getAccessibleOrder(id);
        return OrderResponse.fromEntity(order);
    }

    @GetMapping("/my")
    @Operation(summary = "List current user orders", description = "Returns all orders belonging to the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders loaded", content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class))))
    })
    public List<OrderResponse> myOrders() {
        return orderService.findCurrentUserOrders();
    }

    @org.springframework.web.bind.annotation.PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancels an order when it belongs to the authenticated user or the caller has ROLE_ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order cancelled", content = @Content(schema = @Schema(implementation = WorkflowResult.class))),
            @ApiResponse(responseCode = "400", description = "Order cannot be cancelled or id is invalid", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public WorkflowResult cancel(
            @Parameter(description = "Order identifier", example = "501", required = true)
            @PathVariable @Positive(message = "id must be a positive number") Long id
    ) {
        return orderService.cancelOrderAccessible(id);
    }
}