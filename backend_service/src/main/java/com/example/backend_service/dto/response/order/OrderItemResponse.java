package com.example.backend_service.dto.response.order;

import java.math.BigDecimal;

import com.example.backend_service.model.order.OrderItem;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Integer quantity;
    private BigDecimal price;
    private Long shopId;
    private String shopName;

    public static OrderItemResponse fromEntity(OrderItem item) {
        return OrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImageUrl(item.getProduct().getImageUrl())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .shopId(item.getProduct().getShop() != null ? item.getProduct().getShop().getId() : null)
                .shopName(item.getProduct().getShop() != null ? item.getProduct().getShop().getShopName() : null)
                .build();
    }
}
