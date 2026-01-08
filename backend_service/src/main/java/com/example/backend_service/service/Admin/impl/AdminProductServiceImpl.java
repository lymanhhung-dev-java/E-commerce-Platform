package com.example.backend_service.service.Admin.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend_service.dto.response.product.ProductListResponse;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.model.product.Product;
import com.example.backend_service.repository.ProductRepository;
import com.example.backend_service.service.Admin.AdminProductService;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "ADMIN-PRODUCT-SERVICE")
public class AdminProductServiceImpl implements AdminProductService {

    private final ProductRepository productRepository;
    
    @Override
    public Page<ProductListResponse> getAllProducts(String keyword, Boolean status, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKey = "%" + keyword.trim().toLowerCase() + "%";
                Predicate namePred = cb.like(cb.lower(root.get("name")), likeKey);
                
                Predicate shopPred = cb.like(cb.lower(root.get("shop").get("shopName")), likeKey);
                
                predicates.add(cb.or(namePred, shopPred));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("isActive"), status));
            }
            
            predicates.add(cb.isFalse(root.get("isDeleted")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> productPage = productRepository.findAll(spec, pageable);
        
        return productPage.map(p -> ProductListResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .price(p.getPrice())
                .imageUrl(p.getImageUrl()) 
                .shopName(p.getShop().getShopName())
                .isActive(p.getIsActive())
                .categoryName(p.getCategory().getName())
                .build());
    }

    @Override
    @Transactional
    public void toggleProductStatus(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException("Không tìm thấy sản phẩm với ID: " + productId));
        boolean currentStatus = product.getIsActive() != null && product.getIsActive();
        product.setIsActive(!currentStatus);
        productRepository.save(product);
    }
}