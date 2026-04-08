package com.example.backend_service.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.model.order.Refund;
import com.example.backend_service.service.Admin.AdminRefundService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
@Tag(name = "Admin Refund Controller", description = "Quản lý hoàn tiền")
public class AdminRefundController {

    private final AdminRefundService adminRefundService;

    @Operation(summary = "Lấy danh sách hoàn tiền PENDING")
    @GetMapping("/pending")
    public ResponseEntity<List<Refund>> getPendingRefunds() {
        return ResponseEntity.ok(adminRefundService.getPendingRefunds());
    }

    @Operation(summary = "Xác nhận đã chuyển khoản và hoàn tất Refund")
    @PostMapping("/{refundId}/confirm")
    public ResponseEntity<String> confirmRefund(@PathVariable Long refundId) {
        adminRefundService.confirmRefund(refundId);
        return ResponseEntity.ok("Xác nhận hoàn tiền thành công!");
    }
}
