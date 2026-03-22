package com.example.backend_service.controller.business;

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
@RequestMapping("/api/merchant/vouchers")
@RequiredArgsConstructor
@Tag(name = "Merchant Voucher Controller", description = "APIs for merchant voucher management")
public class MerchantVoucherController {

    private final VoucherService voucherService;

    @Operation(summary = "Get Merchant Vouchers", description = "Lấy danh sách voucher do Shop tạo")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<Page<VoucherResponse>> getShopVouchers(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(voucherService.getShopVouchers(keyword, pageable));
    }

    @Operation(summary = "Lấy chi tiết voucher của shop")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<VoucherResponse> getVoucherById(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getVoucherById(id, true));
    }

    @Operation(summary = "Shop tạo voucher mới")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<VoucherResponse> createShopVoucher(
            @Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.ok(voucherService.createShopVoucher(request));
    }

    @Operation(summary = "Shop cập nhật voucher")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<VoucherResponse> updateShopVoucher(
            @PathVariable Long id,
            @Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.ok(voucherService.updateShopVoucher(id, request));
    }

    @Operation(summary = "Shop xóa voucher")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<String> deleteShopVoucher(@PathVariable Long id) {
        voucherService.deleteShopVoucher(id);
        return ResponseEntity.ok("Xóa voucher thành công");
    }

    @Operation(summary = "Bật/Tắt hoạt động của voucher")
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<String> toggleShopVoucherStatus(@PathVariable Long id) {
        voucherService.toggleShopVoucherStatus(id);
        return ResponseEntity.ok("Thay đổi trạng thái thành công");
    }
}
