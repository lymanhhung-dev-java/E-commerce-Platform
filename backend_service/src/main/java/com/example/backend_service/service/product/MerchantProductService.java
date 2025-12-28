package com.example.backend_service.service.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.backend_service.dto.request.product.MerchantProductCreateRequest;
import com.example.backend_service.dto.request.product.MerchantProductUpdateRequest;
import com.example.backend_service.dto.response.product.MerchantProductResponse;
import com.example.backend_service.dto.response.product.ProductDetailResponse;

public interface MerchantProductService {

    ProductDetailResponse create(MerchantProductCreateRequest request);

    ProductDetailResponse update(Long id, MerchantProductUpdateRequest request);

    void softDelete(Long id);

    void toggleProductStatus(Long id);

    Page<MerchantProductResponse> getMerchantProducts(String keyword, Long categoryId, Boolean status, Pageable pageable);
}
