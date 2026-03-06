package ru.gigasigma.blpscrud.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.enums.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUserId(Long userId);
    List<Order> findAllByStatus(OrderStatus status);
}
