package com.example.backend_service.dto.request.business;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Setter
@Getter
public class WithdrawRequest {
    @NotNull(message = "Số tiền rút không được để trống")
    @Min(value = 50000, message = "Số tiền rút tối thiểu là 50.000 VNĐ")
    private BigDecimal amount;

    @NotBlank(message = "Số tài khoản không được để trống")
    private String accountNumber;

    private String accountName;
}
