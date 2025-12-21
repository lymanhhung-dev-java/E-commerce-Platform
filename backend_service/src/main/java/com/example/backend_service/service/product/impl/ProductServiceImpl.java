package com.example.backend_service.service.product.impl;

import com.example.backend_service.model.product.Product;
import com.example.backend_service.repository.ProductRepository;
import com.example.backend_service.repository.specification.ProductSpecification;
import com.example.backend_service.service.product.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "PRODUCT-SERVICE")
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;


    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
    
    @Override
    public Page<Product> getProducts(String search, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable) {
        Specification<Product> spec = Specification.where(ProductSpecification.isActive());
        if(search != null &&  !search.isEmpty()){
            spec = spec.and(ProductSpecification.hasName(search));
        }
        if(categoryId != null){
            spec = spec.and(ProductSpecification.hasCategory(categoryId));
        }
        if(minPrice != null || maxPrice != null){
            spec = spec.and(ProductSpecification.hasPriceRange(minPrice, maxPrice));
        }
        return productRepository.findAll(spec, pageable);

    }
}

