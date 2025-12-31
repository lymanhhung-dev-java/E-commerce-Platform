package com.example.backend_service.dto.response.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SepayTransactionDto {
    private String id;
    
    @JsonProperty("bank_brand_name")
    private String bankBrandName;
    
    @JsonProperty("account_number")
    private String accountNumber;
    
    @JsonProperty("transaction_date")
    private String transactionDate;
    
    @JsonProperty("amount_in")
    private BigDecimal amountIn; // Số tiền vào
    
    @JsonProperty("transaction_content")
    private String transactionContent; // Nội dung chuyển khoản
    
    @JsonProperty("reference_number")
    private String referenceNumber;
}
