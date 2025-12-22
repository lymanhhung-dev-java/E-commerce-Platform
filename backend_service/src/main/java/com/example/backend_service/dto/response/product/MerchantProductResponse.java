package com.example.backend_service.dto.response.product;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class MerchantProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String imageUrl;
    private boolean isActive;
}

