package com.example.backend_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend_service.model.business.Shop;
import com.example.backend_service.model.order.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByShop(Shop shop, Pageable pageable);
}
