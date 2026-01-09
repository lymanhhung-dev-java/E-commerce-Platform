package com.example.backend_service.controller.order;

import com.example.backend_service.dto.request.cart.CartItemRequest;
import com.example.backend_service.dto.response.order.CartItemResponse;
import com.example.backend_service.service.business.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Slf4j(topic = "CART-MANAGEMENT-CONTROLLER")
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // 1. Thêm vào giỏ hàng 
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestBody CartItemRequest request) {
        cartService.addToCart(request);
        return ResponseEntity.ok("Them vao gio hang thanh cong");
    }

    // 2. Xem giỏ hàng (GET)
    @GetMapping
    public ResponseEntity<List<CartItemResponse>>getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    } 

    // 3. Cập nhật số lượng (PUT)
    @PutMapping("/update")
    public ResponseEntity<String> updateQuantity(@RequestBody CartItemRequest request) {
        cartService.updateQuantity(request);
        return ResponseEntity.ok("Đã cập nhật số lượng thành công");
    }

    // 4. Xóa sản phẩm khỏi giỏ (DELETE)
    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFromCart(@RequestParam Long productId) {
        cartService.removeFromCart(productId);
        return ResponseEntity.ok("Xoá thành công");
    }

    
}