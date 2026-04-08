package com.example.backend_service.dto.request.support;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportCreateRequest {
    private Long shopId;
    private Long orderId;

    @NotBlank(message = "Lý do khiếu nại không được để trống")
    private String reasonType;

    @NotBlank(message = "Nội dung chi tiết không được để trống")
    private String description;
}
