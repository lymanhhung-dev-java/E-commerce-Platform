package com.example.backend_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend_service.model.order.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>{
    
}
