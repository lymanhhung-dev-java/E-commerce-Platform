package com.example.backend_service.controller.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.dto.request.order.CheckoutRequest;
import com.example.backend_service.service.order.CheckoutService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/checkout")
@Tag(name = "Checkout Controller", description = "Checkout / đặt hàng")
@Slf4j(topic = "CHECKOUT-CONTROLLER")
public class CheckoutController {

    private final CheckoutService checkoutService;

    @Operation(summary = "Checkout - Tạo đơn hàng từ giỏ hàng hoặc list sản phẩm")
    @PostMapping
    public ResponseEntity<Object> checkout(@RequestBody @Valid CheckoutRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Checkout request by {} with {} items", username, req.getItems() != null ? req.getItems().size() : 0);
        Long orderId = checkoutService.checkout(req, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of("orderId", orderId));
    }
}
