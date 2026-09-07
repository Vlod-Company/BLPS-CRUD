package ru.gigasigma.blpscrud.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.gigasigma.blpscrud.controller.dto.response.OrderResponse;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.service.OrderService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable @Positive(message = "id must be a positive number") Long id) {
        Order order = orderService.getAccessibleOrder(id);
        return OrderResponse.fromEntity(order);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> findAll() {
        return orderService.findAllOrders();
    }

    @GetMapping("/my")
    public List<OrderResponse> myOrders() {
        return orderService.findCurrentUserOrders();
    }

    @PostMapping("/{id}/cancel")
    public WorkflowResult cancel(@PathVariable @Positive(message = "id must be a positive number") Long id) {
        return orderService.cancelOrderAccessible(id);
    }
}
