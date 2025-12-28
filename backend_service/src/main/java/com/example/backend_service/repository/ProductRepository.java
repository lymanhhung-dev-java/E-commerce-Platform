package com.example.backend_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import com.example.backend_service.model.product.Product;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT p FROM Product p WHERE " +
           "p.shop.owner.username = :ownerUsername AND " + 
           "p.isDeleted = false AND " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " + // <--- Đã xóa đoạn check SKU ở dòng này
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:status IS NULL OR p.isActive = :status)")
    Page<Product> findProductsForMerchant(
            @Param("ownerUsername") String ownerUsername,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") Boolean status,
            Pageable pageable
    );
}
