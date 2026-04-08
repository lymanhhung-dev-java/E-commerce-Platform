package com.example.backend_service.service.support.impl;

import com.example.backend_service.common.ComplaintStatus;
import com.example.backend_service.dto.request.support.ReportCreateRequest;
import com.example.backend_service.dto.response.support.ReportResponse;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.business.Shop;
import com.example.backend_service.model.order.Order;
import com.example.backend_service.model.support.SystemReport;
import com.example.backend_service.repository.OrderRepository;
import com.example.backend_service.repository.ShopRepository;
import com.example.backend_service.repository.SystemReportRepository;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.service.support.SystemReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemReportServiceImpl implements SystemReportService {

    private final SystemReportRepository systemReportRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void createReport(String username, ReportCreateRequest request) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng");
        }

        Shop shop = null;
        if (request.getShopId() != null) {
            shop = shopRepository.findById(request.getShopId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Shop"));
        }

        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Đơn hàng"));
        }

        SystemReport report = new SystemReport();
        report.setUser(user);
        report.setShop(shop);
        report.setOrder(order);
        report.setReasonType(request.getReasonType());
        report.setDescription(request.getDescription());
        report.setStatus(ComplaintStatus.PENDING);

        systemReportRepository.save(report);
    }

    @Override
    public List<ReportResponse> getAllReports() {
        return systemReportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void resolveReport(Long id, String adminNote, String resolution) {
        SystemReport report = systemReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khiếu nại"));

        ComplaintStatus status = ComplaintStatus.valueOf(resolution.toUpperCase());
        report.setStatus(status);
        report.setAdminNote(adminNote);

        systemReportRepository.save(report);
    }
}
