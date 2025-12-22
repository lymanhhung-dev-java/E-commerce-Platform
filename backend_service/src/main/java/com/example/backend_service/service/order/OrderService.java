package com.example.backend_service.service.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.backend_service.common.OrderStatus;
import com.example.backend_service.dto.response.order.OrderResponse;

public interface OrderService {
    Page<OrderResponse> getOrdersByShop(Pageable pageable);
    
    void updateOrderStatus(Long orderId, OrderStatus status);
}
