package com.example.backend_service.dto.request.product;

import lombok.Data;

@Data
public class CategoryRequest {
    private String name;
    private Long parentId;
    private Boolean isActive;
}
