package com.example.backend_service.service.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.backend_service.dto.response.product.ProductListResponse;

public interface AdminProductService {
    Page<ProductListResponse> getAllProducts(String keyword, Boolean status, Pageable pageable);
    void toggleProductStatus(Long productId);
}
