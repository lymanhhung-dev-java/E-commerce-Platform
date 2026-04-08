package com.example.backend_service.controller.business;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.dto.response.statistic.StatisticResponse;
import com.example.backend_service.dto.response.statistic.FinancialReportResponse;
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

    @Operation(summary = "Báo cáo tài chính chi tiết",
               description = "Trả về tổng doanh thu gốc, tổng tiền giảm Voucher Shop, tổng phí sàn, và số tiền thực tế cộng vào ví. (Thực nhận = Giá gốc - Voucher Shop - Phí sàn)")
    @GetMapping("/financial-report")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<FinancialReportResponse> getFinancialReport() {
        return ResponseEntity.ok(statisticService.getFinancialReport());
    }

    @Operation(summary = "Báo cáo tài chính tháng",
               description = "Trả về báo cáo tài chính cho tháng cụ thể: doanh thu gốc, tiền giảm Voucher Shop, phí sàn, và lợi nhuận ròng thực nhận. " +
                             "Truyền month và year (không bắt buộc, mặc định là tháng/năm hiện tại).")
    @GetMapping("/financial-report/monthly")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<FinancialReportResponse> getMonthlyFinancialReport(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(statisticService.getMonthlyFinancialReport(month, year));
    }

    @Operation(summary = "Lấy dữ liệu Tương tác Giao diện Dashboard (Visibility)",
               description = "Trả về doanh thu hôm nay, số đơn hôm nay, và các cảnh báo To-do list (đơn chờ, hàng sắp hết...).")
    @GetMapping("/dashboard-actions")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<com.example.backend_service.dto.response.statistic.MerchantDashboardActionResponse> getDashboardActionMetrics() {
        return ResponseEntity.ok(statisticService.getDashboardActionMetrics());
    }
}
