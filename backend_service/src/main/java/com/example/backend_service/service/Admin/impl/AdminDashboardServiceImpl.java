package com.example.backend_service.service.Admin.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.example.backend_service.common.ComplaintStatus;
import com.example.backend_service.common.RefundStatus;
import com.example.backend_service.common.ShopStatus;
import com.example.backend_service.common.WithdrawalStatus;
import com.example.backend_service.dto.response.statistic.AdminDashboardResponse;
import com.example.backend_service.repository.OrderRepository;
import com.example.backend_service.repository.RefundRepository;
import com.example.backend_service.repository.ShopRepository;
import com.example.backend_service.repository.SystemReportRepository;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.repository.WithdrawalRepository;
import com.example.backend_service.service.Admin.AdminDashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final RefundRepository refundRepository;
    private final SystemReportRepository systemReportRepository;

    @Override
    public AdminDashboardResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalShops = shopRepository.count();

        long pendingShops = shopRepository.countByStatus(ShopStatus.PENDING);
        long pendingWithdrawals = withdrawalRepository.countByStatus(WithdrawalStatus.PENDING);
        long pendingRefunds = refundRepository.countByStatus(RefundStatus.PENDING);
        long pendingReports = systemReportRepository.countByStatus(ComplaintStatus.PENDING);

        BigDecimal platformRevenue = orderRepository.sumTotalPlatformRevenue();
        if (platformRevenue == null) platformRevenue = BigDecimal.ZERO;

        BigDecimal totalGMV = orderRepository.sumTotalGrossMerchandiseValue();
        if (totalGMV == null) totalGMV = BigDecimal.ZERO;

        BigDecimal escrowBalance = shopRepository.sumAllPendingBalances();
        if (escrowBalance == null) escrowBalance = BigDecimal.ZERO;

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalShops(totalShops)
                .pendingShopRequests(pendingShops)
                .pendingWithdrawals(pendingWithdrawals)
                .pendingRefunds(pendingRefunds)
                .pendingReports(pendingReports)
                .totalRevenue(platformRevenue)
                .totalGMV(totalGMV)
                .escrowBalance(escrowBalance)
                .build();
    }
}

 

