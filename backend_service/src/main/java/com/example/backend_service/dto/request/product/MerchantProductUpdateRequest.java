package com.example.backend_service.dto.request.product;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MerchantProductUpdateRequest {

    private String name;

    private BigDecimal price;

    private Integer stock;

    private String description;

    private String image;

    private Boolean isActive;

    private Long categoryId;

    private List<String> detailImages;

}
    

