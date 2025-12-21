package com.example.backend_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend_service.model.order.CartItem;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.product.Product;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    List<CartItem> findByUserAndProductIn(User user, List<Product> products);
    // Tìm tất cả món hàng trong giỏ của user cụ thể
    // Lưu ý: Mình giả định trong CartItem có trường 'user' hoặc 'userId'
    List<CartItem> findByUserId(Long userId);

    // Tìm món hàng cụ thể của user (để kiểm tra trùng khi thêm mới)
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    Optional<CartItem> findByUserAndProduct(User user, Product product);

    void deleteByUserAndProduct(User user, Product product);
}
