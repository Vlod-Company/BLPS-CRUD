package ru.gigasigma.blpscrud.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.enums.OrderStatus;
import ru.gigasigma.blpscrud.service.OrderService;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PendingOrderTimeoutScheduler {

    private final OrderService orderService;

    @Scheduled(fixedDelayString = "300000")
    public void refreshFlights() {
        var pendingOrders = orderService.findAllOrders()
                .stream()
                .filter(order -> order.status().equals(OrderStatus.PENDING))
                .toList();

        for (var order : pendingOrders) {
            if (order.createdAt().plusMinutes(5).isBefore(LocalDateTime.now())) {
                orderService.cancelOrder(order.id());
            }
        }
    }
}
