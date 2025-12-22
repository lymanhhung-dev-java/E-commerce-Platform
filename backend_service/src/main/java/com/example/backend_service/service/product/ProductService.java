package com.example.backend_service.service.product;

import com.example.backend_service.model.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;

public interface ProductService {
    Product getProductById(Long id);
    Page<Product> getProducts(String search, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}
