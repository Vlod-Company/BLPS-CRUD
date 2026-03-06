package ru.gigasigma.blpscrud.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.gigasigma.blpscrud.controller.dto.OrderResponse;
import ru.gigasigma.blpscrud.controller.dto.ProcessPaymentRequest;
import ru.gigasigma.blpscrud.controller.dto.StartPurchaseRequest;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.repository.OrderRepository;
import ru.gigasigma.blpscrud.service.PurchaseWorkflowService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PurchaseWorkflowService purchaseWorkflowService;
    private final OrderRepository orderRepository;

    @PostMapping
    public OrderResponse create(@RequestBody StartPurchaseRequest request) {
        WorkflowResult result = purchaseWorkflowService.startInternalPurchase(request);
        Order order = orderRepository.findById(result.orderId())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Order not found: " + result.orderId()));
        return OrderResponse.fromEntity(order);
    }

    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Order not found: " + id));
        return OrderResponse.fromEntity(order);
    }

    @GetMapping("/my")
    public List<OrderResponse> myOrders(@RequestParam Long userId) {
        return orderRepository.findAllByUserId(userId)
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @PostMapping("/{id}/pay")
    public WorkflowResult pay(@PathVariable Long id, @RequestBody ProcessPaymentRequest request) {
        return purchaseWorkflowService.processInternalPayment(id, request);
    }

    @PostMapping("/{id}/cancel")
    public WorkflowResult cancel(@PathVariable Long id) {
        return purchaseWorkflowService.cancelOrder(id);
    }
}
