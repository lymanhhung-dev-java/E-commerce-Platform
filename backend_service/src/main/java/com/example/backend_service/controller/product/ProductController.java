package com.example.backend_service.controller.product;

import com.example.backend_service.dto.response.product.ProductDetailResponse;
import com.example.backend_service.model.product.Product;
import com.example.backend_service.service.product.ProductService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Slf4j(topic = "PRODUCT-CONTROLLER")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 2. API Xem chi tiết 1 sản phẩm
    // URL ví dụ: GET /api/products/5
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductDetail(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ProductDetailResponse.fromEntity(product));
    }

}