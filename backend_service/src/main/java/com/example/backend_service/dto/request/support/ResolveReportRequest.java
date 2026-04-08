package com.example.backend_service.dto.request.support;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResolveReportRequest {
    @NotBlank(message = "Kết quả giải quyết không được để trống")
    private String resolution; // RESOLVED, REJECTED
    
    private String adminNote;
}
