package com.example.backend_service.controller.admin;

import com.example.backend_service.dto.request.voucher.VoucherRequest;
import com.example.backend_service.dto.response.voucher.VoucherResponse;
import com.example.backend_service.service.voucher.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
@Tag(name = "Admin Voucher Controller", description = "Admin quản lý voucher hệ thống")
public class AdminVoucherController {

    private final VoucherService voucherService;

    @Operation(summary = "Lấy danh sách voucher", description = "Hỗ trợ lọc theo mã voucher và phân trang")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<VoucherResponse>> getVouchers(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(voucherService.getAllVouchers(keyword, pageable));
    }

    @Operation(summary = "Lấy chi tiết voucher")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<VoucherResponse> getVoucherById(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getVoucherById(id, false));
    }

    @Operation(summary = "Tạo hệ thống voucher mới")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<VoucherResponse> createVoucher(@Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.ok(voucherService.createSystemVoucher(request));
    }

    @Operation(summary = "Cập nhật voucher hệ thống")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<VoucherResponse> updateVoucher(
            @PathVariable Long id, 
            @Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.ok(voucherService.updateSystemVoucher(id, request));
    }

    @Operation(summary = "Xóa voucher hệ thống")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteSystemVoucher(id);
        return ResponseEntity.ok("Xóa voucher hệ thống thành công");
    }
}
