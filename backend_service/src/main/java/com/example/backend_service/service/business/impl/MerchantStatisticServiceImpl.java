package com.example.backend_service.service.business.impl;

import com.example.backend_service.dto.response.statistic.StatisticResponse;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.repository.OrderRepository;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.service.business.MerchantStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MerchantStatisticServiceImpl implements MerchantStatisticService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    public List<StatisticResponse> getRevenueStatistics(String type, Integer month, Integer year) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User merchant = userRepository.findByUsername(username);
        if (merchant.getShop() == null) {
            throw new RuntimeException("Tài khoản này chưa đăng ký Shop");
        }
        Long shopId = merchant.getShop().getId();

        int currentYear = (year != null) ? year : LocalDate.now().getYear();
        int currentMonth = (month != null) ? month : LocalDate.now().getMonthValue();

        List<Object[]> rawData;
        List<StatisticResponse> result = new ArrayList<>();

        switch (type.toUpperCase()) {
            case "MONTH": 
                rawData = orderRepository.findRevenueByMonth(shopId, currentMonth, currentYear);
                
                int daysInMonth = YearMonth.of(currentYear, currentMonth).lengthOfMonth();
                result = fillMissingData(rawData, 1, daysInMonth, false);
                break;

            case "YEAR": 
                rawData = orderRepository.findRevenueByYear(shopId, currentYear);
                
                result = fillMissingData(rawData, 1, 12, true);
                break;

            case "WEEK":
                LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
                LocalDateTime start = end.minusDays(6).with(LocalTime.MIN);
                rawData = orderRepository.findRevenueByDateRange(shopId, start, end);

                result = fillMissingDaysForWeek(rawData, start.toLocalDate(), end.toLocalDate());
                break;

            default:
                throw new RuntimeException("Loại thống kê không hợp lệ");
        }

        return result;
    }

    private List<StatisticResponse> fillMissingData(List<Object[]> rawData, int start, int end, boolean isMonth) {
        
        Map<Integer, BigDecimal> mapData = new HashMap<>();
        for (Object[] row : rawData) {
            Integer timePoint = (Integer) row[0]; 
            BigDecimal amount = (BigDecimal) row[1]; 
            mapData.put(timePoint, amount);
        }

        List<StatisticResponse> fullList = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            String label = isMonth ? "Tháng " + i : String.valueOf(i);
            BigDecimal value = mapData.getOrDefault(i, BigDecimal.ZERO);
            fullList.add(new StatisticResponse(label, value));
        }
        return fullList;
    }

    private List<StatisticResponse> fillMissingDaysForWeek(List<Object[]> rawData, LocalDate start, LocalDate end) {
        Map<String, BigDecimal> mapData = new HashMap<>();
        for (Object[] row : rawData) {
            String dateKey = row[0].toString(); 
            BigDecimal amount = (BigDecimal) row[1];
            mapData.put(dateKey, amount);
        }

        List<StatisticResponse> fullList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM"); 

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            String key = date.toString(); 
            String label = date.format(formatter); 

            BigDecimal value = mapData.getOrDefault(key, BigDecimal.ZERO);
            fullList.add(new StatisticResponse(label, value));
        }
        return fullList;
    }
}