package com.example.backend_service.service.product;

import com.example.backend_service.model.product.Product;

public interface ProductService {
    Product getProductById(Long id);
}