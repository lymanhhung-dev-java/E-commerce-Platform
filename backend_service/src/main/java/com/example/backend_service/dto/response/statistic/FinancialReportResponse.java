package com.example.backend_service.dto.response.statistic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialReportResponse {
    private BigDecimal totalOriginalRevenue;
    private BigDecimal totalShopVoucherDiscount;
    private BigDecimal totalCommissionFee;
    private BigDecimal actualBalanceAdded;
}
