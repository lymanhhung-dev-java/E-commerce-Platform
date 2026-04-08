package com.example.backend_service.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentQrResponse {
    private String qrUrl;
    private String bankName;
    private String accountNo;
    private String accountName;
    private BigDecimal amount;
    private String content;
}
