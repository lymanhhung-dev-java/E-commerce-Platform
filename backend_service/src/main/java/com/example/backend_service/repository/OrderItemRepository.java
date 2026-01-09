package com.example.backend_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend_service.model.order.Order;
import com.example.backend_service.model.order.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>{

    List<OrderItem> findByOrder(Order order);
}
