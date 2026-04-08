package com.example.backend_service.dto.response.statistic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantDashboardActionResponse {
    private Double todayRevenue;
    private Long newOrdersToday;
    private Long lowStockProductCount;
    private Long pendingOrderCount;
    private Long unreadMessageCount;
    private Double frozenBalance;
    private Double availableBalance;
}
