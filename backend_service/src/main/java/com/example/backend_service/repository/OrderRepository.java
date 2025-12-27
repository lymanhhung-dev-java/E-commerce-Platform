package com.example.backend_service.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.model.order.Order;

public interface OrderRepository extends JpaRepository<Order, Long>,JpaSpecificationExecutor<Order>{
    Page<Order> findByShop(Shop shop, Pageable pageable);

    List<Order> findByUserOrderByCreatedAtDesc(User user);
}
