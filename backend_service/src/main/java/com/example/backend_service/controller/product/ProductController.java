package com.example.backend_service.controller.product;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.dto.response.product.ProductDetailResponse;
import com.example.backend_service.dto.response.product.ProductListResponse;
import com.example.backend_service.model.product.Product;
import com.example.backend_service.service.product.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j(topic = "PRODUCT-CONTROLLER")
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductListResponse>> getProducts(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @PageableDefault(size = 10,sort = "createdAt",  direction = Sort.Direction.DESC) Pageable pageable
    ){
        var productPage= productService.getProducts(search, categoryId, minPrice, maxPrice, pageable);

        Page<ProductListResponse> response = productPage.map(ProductListResponse::fromEntity);
        return ResponseEntity.ok(response);
    }

     @GetMapping("/{id}")
    public ResponseEntity<?> getProductDetail(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ProductDetailResponse.fromEntity(product));
    }

}
