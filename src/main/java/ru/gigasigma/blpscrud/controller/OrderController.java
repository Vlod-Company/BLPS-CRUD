package ru.gigasigma.blpscrud.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.gigasigma.blpscrud.controller.dto.OrderResponse;
import ru.gigasigma.blpscrud.controller.dto.PaymentRedirectResponse;
import ru.gigasigma.blpscrud.controller.dto.StartPurchaseRequest;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.service.InternalPurchaseService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;
import ru.gigasigma.blpscrud.service.impl.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final InternalPurchaseService internalPurchaseService;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid StartPurchaseRequest request) {
        PaymentRedirectResponse redirect = internalPurchaseService.startInternalPurchase(request);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(redirect.redirectUrl()))
                .build();
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable @Positive(message = "id must be a positive number") Long id) {
        Order order = orderService.getOrder(id);
        return OrderResponse.fromEntity(order);
    }

    @GetMapping("/my")
    public List<OrderResponse> myOrders(@RequestParam @Positive(message = "userId must be a positive number") Long userId) {
        return orderService.findAllByUserId(userId);
    }

    @PostMapping("/{id}/cancel")
    public WorkflowResult cancel(@PathVariable @Positive(message = "id must be a positive number") Long id) {
        return orderService.cancelOrder(id);
    }
}
