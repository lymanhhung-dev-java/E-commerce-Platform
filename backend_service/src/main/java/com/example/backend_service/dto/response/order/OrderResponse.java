package com.example.backend_service.dto.response.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.example.backend_service.common.OrderStatus;
import com.example.backend_service.model.order.Order;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String customerName; 
    private String shippingAddress;
    private String shippingPhone;
    private String note;
    private String paymentMethod;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    public static OrderResponse fromEntity(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerName(order.getUser().getFullName())
                .shippingAddress(order.getShippingAddress())
                .shippingPhone(order.getShippingPhone())
                .note(order.getNote())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .items(order.getOrderItems().stream()
                        .map(OrderItemResponse::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
    
}
