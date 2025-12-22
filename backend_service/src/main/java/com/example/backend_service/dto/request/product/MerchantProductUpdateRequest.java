package com.example.backend_service.dto.request.product;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MerchantProductUpdateRequest {

    private String name;

    private BigDecimal price;

    private Integer stock;

    private String description;

    private String image;

    private Boolean isActive;

    private Long categoryId;
}

