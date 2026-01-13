package com.example.backend_service.dto.response.wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.backend_service.common.WithdrawalStatus;
import com.example.backend_service.model.business.Withdrawal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalResponse {
    private Long id;
    private BigDecimal amount;
    private String bankName;
    private String accountNumber;
    private String accountName;
    private WithdrawalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WithdrawalResponse fromEntity(Withdrawal withdrawal) {
        return WithdrawalResponse.builder()
                .id(withdrawal.getId())
                .amount(withdrawal.getAmount())
                .bankName(withdrawal.getBankName())
                .accountNumber(withdrawal.getAccountNumber())
                .accountName(withdrawal.getAccountName())
                .status(withdrawal.getStatus())
                .createdAt(withdrawal.getCreatedAt())
                .updatedAt(withdrawal.getUpdatedAt())
                .build();
    }
}
