package com.example.backend_service.dto.request.wallet;

import com.example.backend_service.common.WithdrawalStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUpdateWithdrawalRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private WithdrawalStatus status; 

    private String rejectReason;
}
