package com.example.backend_service.dto.response.business;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletOverviewResponse {
    
    private BigDecimal totalRevenue;

    private BigDecimal periodRevenue;

    private BigDecimal currentBalance;
}
