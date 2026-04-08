package com.example.backend_service.dto.response.support;

import com.example.backend_service.common.ComplaintStatus;
import com.example.backend_service.model.support.SystemReport;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {
    private Long id;
    private String username;
    private String shopName;
    private Long orderId;
    private String reasonType;
    private String description;
    private ComplaintStatus status;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReportResponse fromEntity(SystemReport r) {
        return ReportResponse.builder()
                .id(r.getId())
                .username(r.getUser() != null ? r.getUser().getUsername() : null)
                .shopName(r.getShop() != null ? r.getShop().getShopName() : null)
                .orderId(r.getOrder() != null ? r.getOrder().getId() : null)
                .reasonType(r.getReasonType())
                .description(r.getDescription())
                .status(r.getStatus())
                .adminNote(r.getAdminNote())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
