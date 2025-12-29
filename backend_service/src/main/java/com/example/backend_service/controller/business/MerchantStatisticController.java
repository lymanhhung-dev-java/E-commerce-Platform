package com.example.backend_service.controller.business;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.dto.response.statistic.StatisticResponse;
import com.example.backend_service.service.business.MerchantStatisticService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/merchant/statistics")
@RequiredArgsConstructor
@Tag(name = "Merchant Statistic Controller", description = "APIs for merchant statistic management")
public class MerchantStatisticController {
    private final MerchantStatisticService statisticService;

    @Operation(summary = "Lấy thống kê doanh thu", 
               description = "Params: type (WEEK, MONTH, YEAR). Với MONTH/YEAR có thể truyền thêm param month/year.")
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<List<StatisticResponse>> getRevenueStats(
            @RequestParam String type, // WEEK, MONTH, YEAR
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(statisticService.getRevenueStatistics(type, month, year));
    }
}
