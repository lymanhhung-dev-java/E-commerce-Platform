package com.example.backend_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend_service.model.order.CartItem;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.product.Product;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    List<CartItem> findByUserAndProductIn(User user, List<Product> products);
}
