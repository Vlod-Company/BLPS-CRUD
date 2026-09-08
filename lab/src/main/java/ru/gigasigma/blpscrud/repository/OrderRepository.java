package ru.gigasigma.blpscrud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gigasigma.blpscrud.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUserId(Long userId);
    Optional<Order> findByIdAndUserId(Long id, Long userId);
}
