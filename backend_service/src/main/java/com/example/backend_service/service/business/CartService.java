package com.example.backend_service.service.business;

import com.example.backend_service.dto.request.cart.CartItemRequest;
import com.example.backend_service.dto.response.order.CartItemResponse;
import java.util.List;

public interface CartService {
    void addToCart(CartItemRequest request);
    List<CartItemResponse> getMyCart();
    void updateQuantity(CartItemRequest request);
    void removeFromCart(Long productId);
}