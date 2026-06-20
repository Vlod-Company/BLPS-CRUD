package ru.gigasigma.blpscrud.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.enums.OrderStatus;

@Service
@RequiredArgsConstructor
public class PendingOrderCleanupService {

    private final OrderService orderService;

    public int cancelExpiredPendingOrders() {
        var expiredOrders = orderService.findAllOrders()
                .stream()
                .filter(order -> order.status().equals(OrderStatus.PENDING))
                .filter(order -> order.createdAt().plusMinutes(5).isBefore(LocalDateTime.now()))
                .toList();

        expiredOrders.forEach(order -> orderService.cancelOrder(order.id()));
        return expiredOrders.size();
    }
}
