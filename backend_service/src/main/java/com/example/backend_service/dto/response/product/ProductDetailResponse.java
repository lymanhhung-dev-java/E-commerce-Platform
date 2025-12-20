package com.example.backend_service.dto.response.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.backend_service.model.product.Product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDetailResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private Integer stockQuantity;
    private Long categoryId;
    private String categoryName;
    private Long shopId;
    private String shopName;
    private String shopAvatar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductDetailResponse fromEntity(Product p) {
        return ProductDetailResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .description(p.getDescription())
                .imageUrl(p.getImageUrl())
                .stockQuantity(p.getStockQuantity())
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .shopId(p.getShop().getId())
                .shopName(p.getShop().getShopName())
                .shopAvatar(p.getShop().getLogoUrl())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}