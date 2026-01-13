package com.example.backend_service.controller.merchant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.dto.response.wallet.WithdrawalResponse;
import com.example.backend_service.service.merchant.MerchantWithdrawalService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/merchant/withdrawals")
@RequiredArgsConstructor
@Tag(name = "Merchant Withdrawal Controller", description = "API quản lý lịch sử rút tiền của Shop")
public class MerchantWithdrawalController {

    private final MerchantWithdrawalService merchantWithdrawalService;

    @Operation(summary = "Lấy lịch sử rút tiền", description = "Lấy danh sách các yêu cầu rút tiền của shop đang đăng nhập")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<Page<WithdrawalResponse>> getMyWithdrawalHistory(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(merchantWithdrawalService.getMyWithdrawalHistory(pageable));
    }
}
