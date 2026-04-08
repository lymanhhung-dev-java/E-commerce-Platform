package com.example.backend_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import com.example.backend_service.common.RefundStatus;
import com.example.backend_service.model.order.Refund;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByStatus(RefundStatus status);
}
