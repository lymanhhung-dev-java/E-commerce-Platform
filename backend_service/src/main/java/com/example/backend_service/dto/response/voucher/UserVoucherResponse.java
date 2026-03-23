package com.example.backend_service.dto.response.voucher;

import com.example.backend_service.common.DiscountType;
import com.example.backend_service.common.OwnerType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UserVoucherResponse {
    private Long id;
    private Long voucherId;
    private String code;
    private BigDecimal discountValue;
    private DiscountType discountType;
    private OwnerType ownerType;
    private Long shopId;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isUsed;
    private LocalDateTime savedAt;
}
