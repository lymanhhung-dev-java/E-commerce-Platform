package com.example.backend_service.dto.response.order;

import lombok.Data;

@Data
public class SepayResponse {
    private int status;
    private String error;
    private java.util.List<SepayTransactionDto> transactions;
}
