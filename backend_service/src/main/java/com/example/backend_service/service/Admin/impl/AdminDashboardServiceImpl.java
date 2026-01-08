package com.example.backend_service.service.Admin.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.example.backend_service.common.ShopStatus;
import com.example.backend_service.common.WithdrawalStatus;
import com.example.backend_service.dto.response.statistic.AdminDashboardResponse;
import com.example.backend_service.repository.OrderRepository;
import com.example.backend_service.repository.ShopRepository;
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

    @Override
    public AdminDashboardResponse getDashboardStats() {
      long totalUsers = userRepository.count();
        long totalShops = shopRepository.count();
    
        long pendingShops = shopRepository.countByStatus(ShopStatus.PENDING); 
        long pendingWithdrawals = withdrawalRepository.countByStatus(WithdrawalStatus.PENDING);
        
        BigDecimal revenue = orderRepository.sumTotalPlatformRevenue();
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalShops(totalShops)
                .pendingShopRequests(pendingShops)
                .pendingWithdrawals(pendingWithdrawals)
                .totalRevenue(revenue)
                .build();
    }
}

 

