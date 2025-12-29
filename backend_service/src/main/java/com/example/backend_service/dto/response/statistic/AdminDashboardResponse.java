package com.example.backend_service.dto.response.statistic;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalShops;
    private long pendingShopRequests; 
    private long pendingWithdrawals;
    private BigDecimal totalRevenue;
}
