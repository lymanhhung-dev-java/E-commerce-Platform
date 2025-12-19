package com.example.backend_service.repository;

// 👇 Import đúng đường dẫn model của bạn
import com.example.backend_service.model.order.CartItem; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Tìm tất cả món hàng trong giỏ của user cụ thể
    // Lưu ý: Mình giả định trong CartItem có trường 'user' hoặc 'userId'
    List<CartItem> findByUserId(Long userId);

    // Tìm món hàng cụ thể của user (để kiểm tra trùng khi thêm mới)
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
}