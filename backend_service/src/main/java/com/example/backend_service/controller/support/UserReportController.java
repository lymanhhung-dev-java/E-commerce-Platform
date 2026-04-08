package com.example.backend_service.controller.support;

import com.example.backend_service.dto.request.support.ReportCreateRequest;
import com.example.backend_service.service.support.SystemReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "User Report Controller", description = "User gửi khiếu nại")
public class UserReportController {

    private final SystemReportService reportService;

    @Operation(summary = "Gửi khiếu nại mới")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> createReport(@Valid @RequestBody ReportCreateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        reportService.createReport(authentication.getName(), request);
        return ResponseEntity.ok("Gửi báo cáo gian lận thành công.");
    }
}
