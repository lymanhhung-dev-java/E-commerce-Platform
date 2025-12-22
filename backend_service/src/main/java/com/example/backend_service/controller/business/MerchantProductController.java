package com.example.backend_service.controller.business;

import com.example.backend_service.dto.request.product.MerchantProductCreateRequest;
import com.example.backend_service.dto.request.product.MerchantProductUpdateRequest;
import com.example.backend_service.dto.response.product.ProductDetailResponse;
import com.example.backend_service.service.product.MerchantProductService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/merchant/products")
@RequiredArgsConstructor
@Slf4j(topic = "MERCHANT-PRODUCT-CONTROLLER")
@Tag(name = "Merchant Product Controller", description = "APIs for merchant product management")
public class MerchantProductController {

    private final MerchantProductService merchantProductService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ProductDetailResponse> create(
            @Valid @RequestBody MerchantProductCreateRequest request) {
        return ResponseEntity.ok(merchantProductService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> update(
            @PathVariable Long id,
             @RequestBody MerchantProductUpdateRequest request) {
        return ResponseEntity.ok(merchantProductService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        merchantProductService.softDelete(id);
        return ResponseEntity.ok("Xóa sản phẩm thành công");

    }
}

