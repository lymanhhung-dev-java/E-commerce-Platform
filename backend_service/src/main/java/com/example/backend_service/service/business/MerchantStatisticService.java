package com.example.backend_service.service.business;

import java.util.List;

import com.example.backend_service.dto.response.statistic.StatisticResponse;

public interface MerchantStatisticService {
    List<StatisticResponse> getRevenueStatistics(String type, Integer month, Integer year);

}
