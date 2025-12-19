package com.example.backend_service.controller.business;

import com.example.backend_service.dto.request.business.CartItemRequest;
import com.example.backend_service.service.business.CartService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@Slf4j(topic = "CART-MANAGEMENT-CONTROLLER")
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // 1. Thêm vào giỏ hàng 
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItemRequest request, Principal principal) {
        return ResponseEntity.ok(cartService.addToCart(request, principal.getName()));
    }

    // 2. Xem giỏ hàng (GET)
    @GetMapping("") 
    public ResponseEntity<?> getMyCart(Principal principal) {
        return ResponseEntity.ok(cartService.getMyCart(principal.getName()));
    }

    // 3. Cập nhật số lượng (PUT)
    @PutMapping("/update")
    public ResponseEntity<?> updateQuantity(@RequestBody CartItemRequest request, Principal principal) {
        return ResponseEntity.ok(cartService.updateQuantity(request, principal.getName()));
    }

    // 4. Xóa sản phẩm khỏi giỏ (DELETE)
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestParam Long productId, Principal principal) {
        cartService.removeFromCart(productId, principal.getName());
        return ResponseEntity.ok("Deleted successfully");
    }
}