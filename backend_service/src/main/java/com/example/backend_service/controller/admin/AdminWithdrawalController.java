package com.example.backend_service.controller.admin;

import com.example.backend_service.dto.request.wallet.AdminUpdateWithdrawalRequest;
import com.example.backend_service.model.business.Withdrawal;
import com.example.backend_service.service.wallet.AdminWithdrawalService;
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
@RequestMapping("/api/admin/withdrawals")
@RequiredArgsConstructor
@Tag(name = "Admin Withdrawal Controller", description = "Admin quản lý yêu cầu rút tiền")
public class AdminWithdrawalController {

    private final AdminWithdrawalService adminWithdrawalService;

    @Operation(summary = "Lấy danh sách yêu cầu rút tiền", description = "Có thể lọc theo status (PENDING, APPROVED, REJECTED)")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<Withdrawal>> getWithdrawals(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(adminWithdrawalService.getAllWithdrawals(status, pageable));
    }

    @Operation(summary = "Duyệt hoặc Từ chối yêu cầu", description = "Nếu từ chối, tiền sẽ được hoàn lại ví Shop")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateWithdrawalRequest request
    ) {
        adminWithdrawalService.updateWithdrawalStatus(id, request);
        return ResponseEntity.ok("Cập nhật trạng thái thành công!");
    }
}