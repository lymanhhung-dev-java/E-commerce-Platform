package com.example.backend_service.dto.response.voucher;

import com.example.backend_service.common.DiscountType;
import com.example.backend_service.common.OwnerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherResponse {
    private Long id;
    private String code;
    private BigDecimal discountValue;
    private DiscountType discountType;
    private OwnerType ownerType;
    private Long shopId;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Integer limitUsage;
    private Integer usedCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
