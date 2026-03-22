package com.example.backend_service.service.Admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.backend_service.dto.response.product.ProductListResponse;

public interface AdminProductService {
    Page<ProductListResponse> getAllProducts(String keyword, Boolean status, Long shopId, Double minPrice, Double maxPrice, Pageable pageable);
    void toggleProductStatus(Long productId);
}
