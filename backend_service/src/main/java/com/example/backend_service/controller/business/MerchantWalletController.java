package com.example.backend_service.controller.business;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.backend_service.dto.request.business.WithdrawRequest;
import com.example.backend_service.dto.response.business.WalletOverviewResponse;
import com.example.backend_service.service.business.MerchantWalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/merchant/wallet")
@RequiredArgsConstructor
public class MerchantWalletController {
    
    private final MerchantWalletService walletService;

    @GetMapping("/overview")
    public ResponseEntity<WalletOverviewResponse> getWalletOverview() {
        return ResponseEntity.ok(walletService.getWalletOverview());
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> requestWithdraw(@Valid @RequestBody WithdrawRequest request) {
        walletService.requestWithdraw(request);
        return ResponseEntity.ok("Yêu cầu rút tiền thành công!");
    }
}
