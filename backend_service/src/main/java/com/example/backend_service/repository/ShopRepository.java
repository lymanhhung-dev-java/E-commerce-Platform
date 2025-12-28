package com.example.backend_service.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.backend_service.common.ShopStatus;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.business.Shop;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    boolean existsByOwner(User user);
    
    Page<Shop> findByStatus(ShopStatus status, Pageable pageable);

    Optional<Shop> findByOwner(User user);
    
    @Query("SELECT s FROM Shop s WHERE " +
           "(:keyword IS NULL OR LOWER(s.shopName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(s.owner.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR s.status = :status)")
    Page<Shop> findAllByKeywordAndStatus(String keyword, ShopStatus status, Pageable pageable);

}
