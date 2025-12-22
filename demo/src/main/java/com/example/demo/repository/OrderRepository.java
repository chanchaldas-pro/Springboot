package com.example.demo.repository;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Example custom queries
    List<Order> findByCustomerId(Long customerId);

    List<Order> findByStatus(OrderStatus status);

    Order findByID(Long orderId);
}
