package com.example.backend_service.service.business.impl;

import com.example.backend_service.dto.request.cart.CartItemRequest;
import com.example.backend_service.dto.response.order.CartItemResponse;
import com.example.backend_service.exception.AppException;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "CART-SERVICE")
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username);
    }

    @Override
    public void addToCart(CartItemRequest request) {
        User user = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException("Product not found with id: " + request.getProductId()));

        if (request.getQuantity() > product.getStockQuantity()) {
            throw new AppException("so luong vuot qua so luong trong kho");
        }

        // Kiểm tra xem user đã có sản phẩm này trong giỏ chưa
        Optional<CartItem> existingItem = cartItemRepository.findByUserAndProduct(user, product);

        if (existingItem.isPresent()) {
            // Trường hợp 1: Đã có -> Cộng dồn số lượng
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (newQuantity > product.getStockQuantity()) {
                throw new AppException("so luong vuot qua so luong trong kho");
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            // Trường hợp 2: Chưa có -> Tạo mới item
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            cartItemRepository.save(newItem);
        }
    }

    @Override
    public List<CartItemResponse> getMyCart() {
        User user = getCurrentUser();
        List<CartItem> items = cartItemRepository.findByUserId(user.getId());
        return items.stream()
                .map(CartItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

   

    @Override
    @Transactional
    public void removeFromCart(Long productId) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException("Sản phẩm không tồn tại"));

        cartItemRepository.deleteByUserAndProduct(user, product);
    }

    @Override
    public void updateQuantity(CartItemRequest request) {
        User user = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException("Sản phẩm không tồn tại"));

        CartItem item = cartItemRepository.findByUserAndProduct(user, product)
                .orElseThrow(() -> new AppException("Sản phẩm chưa có trong giỏ hàng"));

        if (request.getQuantity() > product.getStockQuantity()) {
            throw new AppException("Số lượng yêu cầu vượt quá tồn kho");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
    }
}