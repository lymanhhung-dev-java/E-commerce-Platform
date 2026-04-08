package com.example.backend_service.dto.request.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkUpdateProductRequest {
    private Long id;
    private Double price;
    private Integer stockQuantity;
}
