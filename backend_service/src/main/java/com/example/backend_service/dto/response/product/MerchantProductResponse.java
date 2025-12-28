package com.example.backend_service.dto.response.product;

import java.math.BigDecimal;

import com.example.backend_service.model.product.Product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MerchantProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private boolean isActive;

    public static MerchantProductResponse fromEntity(Product product) {
        return MerchantProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStockQuantity())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .isActive(product.getIsActive())
                .build();
    }
}

