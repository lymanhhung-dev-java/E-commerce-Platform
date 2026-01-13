package com.example.backend_service.service.merchant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.backend_service.dto.response.wallet.WithdrawalResponse;

public interface MerchantWithdrawalService {
    Page<WithdrawalResponse> getMyWithdrawalHistory(Pageable pageable);
}
