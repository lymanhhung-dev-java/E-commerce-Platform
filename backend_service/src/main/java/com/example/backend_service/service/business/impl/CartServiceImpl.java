package com.example.backend_service.service.business.impl;

import com.example.backend_service.dto.request.business.CartItemRequest;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.order.CartItem;
import com.example.backend_service.model.product.Product;
import com.example.backend_service.repository.CartItemRepository;
import com.example.backend_service.repository.ProductRepository;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.service.business.CartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j(topic = "CART-SERVICE")
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;

    // Hàm phụ trợ: Lấy thông tin User đang đăng nhập
    private User getCurrentUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found: " + username);
        }
        return user;
    }

    @Override
    public CartItem addToCart(CartItemRequest request, String username) {
        User user = getCurrentUser(username);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + request.getProductId()));

        // Kiểm tra xem user đã có sản phẩm này trong giỏ chưa
        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(user.getId(), product.getId());

        if (existingItem.isPresent()) {
            // Trường hợp 1: Đã có -> Cộng dồn số lượng
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            return cartItemRepository.save(item);
        } else {
            // Trường hợp 2: Chưa có -> Tạo mới item
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            // Các trường createdAt sẽ tự động được set nhờ @EntityListeners trong Model của bạn
            return cartItemRepository.save(newItem);
        }
    }

    @Override
    public List<CartItem> getMyCart(String username) {
        User user = getCurrentUser(username);
        return cartItemRepository.findByUserId(user.getId());
    }

    @Override
    public CartItem updateQuantity(CartItemRequest request, String username) {
        User user = getCurrentUser(username);
        
        // Tìm sản phẩm trong giỏ để cập nhật
        CartItem item = cartItemRepository.findByUserIdAndProductId(user.getId(), request.getProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));

        // Cập nhật số lượng mới
        item.setQuantity(request.getQuantity());
        return cartItemRepository.save(item);
    }

    @Override
    public void removeFromCart(Long productId, String username) {
        User user = getCurrentUser(username);
        
        // Tìm sản phẩm để xóa
        CartItem item = cartItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));
        
        cartItemRepository.delete(item);
    }
}