package com.example.backend_service.dto.response.product;

import com.example.backend_service.model.product.Category;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private boolean active;
    private String displayName; 
    private int level;         

    private Long parentId;
    
    private List<CategoryResponse> children; 

    public static CategoryResponse fromEntity(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .imageUrl(category.getImageUrl())
                .active(category.getIsActive())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .build();
    }
}