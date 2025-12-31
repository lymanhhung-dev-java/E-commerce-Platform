package com.example.backend_service.controller.order;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        log.info("Checkout request by {} with {} items", req.getItems() != null ? req.getItems().size() : 0);
        List<Long> orderId = checkoutService.checkout(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of("orderId", orderId));
    }

    @GetMapping("/{orderId}/payment-qr")
    public ResponseEntity<String> getPaymentQr(@PathVariable Long orderId) {
        String url = checkoutService.getPaymentQrUrl(orderId);
        return ResponseEntity.ok(url);
    }

    // 2. API Kiểm tra trạng thái thanh toán (Frontend gọi 5s/lần)
    @GetMapping("/{orderId}/payment-status")
    public ResponseEntity<Boolean> checkPaymentStatus(@PathVariable Long orderId) {
        boolean isPaid = checkoutService.checkPaymentStatus(orderId);
        return ResponseEntity.ok(isPaid);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        checkoutService.cancelOrderAndRestoreCart(orderId);
        return ResponseEntity.ok("Đơn hàng đã bị hủy");
    }
}
