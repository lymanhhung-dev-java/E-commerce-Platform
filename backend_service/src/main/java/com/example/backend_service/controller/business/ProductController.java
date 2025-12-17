package com.example.backend_service.controller.business;

import com.example.backend_service.model.product.Product;
import com.example.backend_service.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 1. API Lấy danh sách sản phẩm (có phân trang)
    // URL ví dụ: GET /api/products?page=0&size=10
    @GetMapping("")
    public ResponseEntity<Page<Product>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Product> products = productService.getAllProducts(page, size);
        return ResponseEntity.ok(products);
    }

    // 2. API Xem chi tiết 1 sản phẩm
    // URL ví dụ: GET /api/products/5
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductDetail(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    // 3. API Tìm kiếm sản phẩm theo tên
    // URL ví dụ: GET /api/products/search?keyword=iphone&page=0&size=10
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Product> result = productService.searchProducts(keyword, page, size);
        return ResponseEntity.ok(result);
    }
}