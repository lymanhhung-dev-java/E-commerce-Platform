package com.example.backend_service.service.wallet;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import com.example.backend_service.dto.request.wallet.AdminUpdateWithdrawalRequest;
import com.example.backend_service.model.business.Withdrawal;

public interface AdminWithdrawalService {
    Page<Withdrawal> getAllWithdrawals(String status, Pageable pageable);
    void updateWithdrawalStatus(Long withdrawalId, AdminUpdateWithdrawalRequest request);
} 