package com.example.backend_service.service.Admin;

import com.example.backend_service.dto.response.statistic.AdminDashboardResponse;

public interface AdminDashboardService {
    AdminDashboardResponse getDashboardStats();
}
