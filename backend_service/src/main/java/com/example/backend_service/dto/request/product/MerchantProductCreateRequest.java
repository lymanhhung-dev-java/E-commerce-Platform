package com.example.backend_service.dto.request.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MerchantProductCreateRequest {

    @NotNull(message = "Category không được để trống")
    private Long categoryId;

    @NotBlank
    private String name;

    @Min(value = 1, message = "Giá phải > 0")
    private BigDecimal price;

    @Min(value = 0, message = "Tồn kho phải >= 0")
    private Integer stock;

    private String description;

    private String mainImageUrl;
    
    private List<String> detailImageUrls;

}
