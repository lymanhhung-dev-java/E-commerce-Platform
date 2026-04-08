package com.example.backend_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.math.BigDecimal;
import com.example.backend_service.model.product.Product;

import jakarta.persistence.LockModeType;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT p FROM Product p WHERE " +
           "p.shop.owner.username = :ownerUsername AND " + 
           "p.isDeleted = false AND " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:status IS NULL OR p.isActive = :status) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:minRating IS NULL OR :minRating = 0.0 OR " +
           "(SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.product = p) >= :minRating)")
    Page<Product> findProductsForMerchant(
            @Param("ownerUsername") String ownerUsername,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") Boolean status,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minRating") Double minRating,
            Pageable pageable
    );

    Long countByShopAndStockQuantityLessThan(com.example.backend_service.model.business.Shop shop, Integer stock);
}
