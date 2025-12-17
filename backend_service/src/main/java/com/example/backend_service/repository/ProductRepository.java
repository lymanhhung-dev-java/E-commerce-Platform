package com.example.backend_service.repository;

import com.example.backend_service.model.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Tìm kiếm sản phẩm theo tên (không phân biệt hoa thường)
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}