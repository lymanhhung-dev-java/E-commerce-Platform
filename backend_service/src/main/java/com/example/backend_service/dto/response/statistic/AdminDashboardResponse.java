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
    private BigDecimal totalRevenue; // This represents Platform revenue
    private BigDecimal totalGMV;     // Tổng giá trị giao dịch của sàn
    private BigDecimal escrowBalance; // Quỹ đang đóng băng
    private long pendingRefunds;     // Chờ hoàn tiền
    private long pendingReports;     // Khiếu nại chờ xử lý
}
