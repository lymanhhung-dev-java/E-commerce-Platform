package com.example.backend_service.service.business;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.backend_service.dto.request.business.WithdrawRequest;
import com.example.backend_service.dto.response.business.WalletOverviewResponse;
import com.example.backend_service.model.business.Withdrawal;

public interface MerchantWalletService {
    WalletOverviewResponse getWalletOverview();

    void requestWithdraw(WithdrawRequest request);

    Page<Withdrawal> getWithdrawalHistory(Pageable pageable);
} 
