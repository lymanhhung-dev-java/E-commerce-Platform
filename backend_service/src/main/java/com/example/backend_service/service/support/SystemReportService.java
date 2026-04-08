package com.example.backend_service.service.support;

import com.example.backend_service.dto.request.support.ReportCreateRequest;
import com.example.backend_service.dto.response.support.ReportResponse;
import java.util.List;

public interface SystemReportService {
    void createReport(String username, ReportCreateRequest request);
    List<ReportResponse> getAllReports();
    void resolveReport(Long id, String adminNote, String resolution); // resolution could be PENDING, RESOLVED, REJECTED 
}
