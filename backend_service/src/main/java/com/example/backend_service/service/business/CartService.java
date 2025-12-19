package com.example.backend_service.service.business;

import com.example.backend_service.dto.request.business.CartItemRequest;
import com.example.backend_service.model.order.CartItem;
import java.util.List;

public interface CartService {
    CartItem addToCart(CartItemRequest request, String username);
    List<CartItem> getMyCart(String username);
    CartItem updateQuantity(CartItemRequest request, String username);
    void removeFromCart(Long productId, String username);
}