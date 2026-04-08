package com.example.backend_service.repository;

import com.example.backend_service.common.ComplaintStatus;
import com.example.backend_service.model.support.SystemReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SystemReportRepository extends JpaRepository<SystemReport, Long> {
    Long countByStatus(ComplaintStatus status);
    List<SystemReport> findAllByOrderByCreatedAtDesc();
}
