package com.example.backend_service.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.backend_service.dto.response.statistic.AdminDashboardResponse;
import com.example.backend_service.service.Admin.AdminDashboardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard Controller", description = "Admin quản lý Dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "Lấy số liệu thống kê Dashboard")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {
        return ResponseEntity.ok(adminDashboardService.getDashboardStats());
    }
}
