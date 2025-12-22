package com.example.backend_service.service.product;

import com.example.backend_service.dto.request.product.MerchantProductCreateRequest;
import com.example.backend_service.dto.request.product.MerchantProductUpdateRequest;
import com.example.backend_service.dto.response.product.ProductDetailResponse;

public interface MerchantProductService {

    ProductDetailResponse create(MerchantProductCreateRequest request);

    ProductDetailResponse update(Long id, MerchantProductUpdateRequest request);

    void softDelete(Long id);
}
