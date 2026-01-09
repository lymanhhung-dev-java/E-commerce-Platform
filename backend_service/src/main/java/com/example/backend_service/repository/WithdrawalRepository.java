package com.example.backend_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend_service.common.WithdrawalStatus;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.model.business.Withdrawal;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long>{
    Page<Withdrawal> findByShop(Shop shop, Pageable pageable);
    Page<Withdrawal> findByStatus(WithdrawalStatus status, Pageable pageable);
    long countByStatus(WithdrawalStatus status);
}
