package com.example.backend_service.controller.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort;
import com.example.backend_service.common.OrderStatus;
import com.example.backend_service.dto.response.order.OrderResponse;
import com.example.backend_service.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j(topic = "ORDER-CONTROLLER")
public class OrderController {

    private final OrderService orderService;
    
    @Operation(summary = "Get My Order History", description = "Lấy lịch sử mua hàng của user đang đăng nhập")
    @GetMapping("/my-orders")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getMyOrders(search, status, pageable));
    }
}
