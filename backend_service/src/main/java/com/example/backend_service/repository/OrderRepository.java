package com.example.backend_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend_service.model.order.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
