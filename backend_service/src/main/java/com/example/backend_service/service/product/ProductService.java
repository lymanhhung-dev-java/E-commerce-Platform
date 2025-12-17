package com.example.backend_service.service.product;

import com.example.backend_service.model.product.Product;
import org.springframework.data.domain.Page;

public interface ProductService {
    Page<Product> getAllProducts(int page, int size);
    Product getProductById(Long id);
    Page<Product> searchProducts(String keyword, int page, int size);
}