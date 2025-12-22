package com.example.backend_service.controller.business;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort;
import com.example.backend_service.common.OrderStatus;
import com.example.backend_service.dto.response.order.OrderResponse;
import com.example.backend_service.service.order.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/merchant/orders")
@RequiredArgsConstructor
@Slf4j(topic = "MERCHANT-ORDER-CONTROLLER")
@Tag(name = "Merchant Order Controller", description = "Quản lý đơn hàng dành cho Shop")
public class MerchantOrderController {
    private final OrderService orderService;

    @Operation(summary = "Get Shop Orders", description = "Xem danh sách đơn hàng của Shop (Phân trang)")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_SELLER')") 
    public ResponseEntity<Page<OrderResponse>> getShopOrders(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.getOrdersByShop(pageable));
    }

    @Operation(summary = "Update Order Status", description = "Cập nhật trạng thái đơn hàng (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELED)")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<String> updateOrderStatus(
            @PathVariable Long id, 
            @RequestParam OrderStatus status 
    ) {
        orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok("Cập nhật trạng thái thành công: " + status);
    }
}
