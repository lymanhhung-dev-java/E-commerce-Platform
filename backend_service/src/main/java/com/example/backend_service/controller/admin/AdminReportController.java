package com.example.backend_service.controller.admin;

import com.example.backend_service.dto.request.support.ResolveReportRequest;
import com.example.backend_service.dto.response.support.ReportResponse;
import com.example.backend_service.service.support.SystemReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Admin Report Controller", description = "Admin xử lý khiếu nại")
public class AdminReportController {

    private final SystemReportService reportService;

    @Operation(summary = "Lấy danh sách tất cả khiếu nại")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @Operation(summary = "Xử lý khiếu nại (Chấp nhận hoặc Từ chối)")
    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> resolveReport(@PathVariable Long id, @Valid @RequestBody ResolveReportRequest request) {
        reportService.resolveReport(id, request.getAdminNote(), request.getResolution());
        return ResponseEntity.ok("Xử lý khiếu nại số " + id + " thành công!");
    }
}
