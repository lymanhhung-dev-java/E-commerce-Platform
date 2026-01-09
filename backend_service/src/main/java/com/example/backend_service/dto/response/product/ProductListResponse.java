package com.example.backend_service.dto.response.product;

import java.math.BigDecimal;

import com.example.backend_service.model.product.Product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductListResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private String categoryName;
    private String shopName;
    private Double rating; 
    private Boolean isActive;
    public static ProductListResponse fromEntity(Product p) {
        return ProductListResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .imageUrl(p.getImageUrl())
                .categoryName(p.getCategory().getName())
                .shopName(p.getShop().getShopName())
                .isActive(p.getIsActive())
                .rating(5.0)
                .build();
    }
}
