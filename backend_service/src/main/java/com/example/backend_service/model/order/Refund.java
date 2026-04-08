package com.example.backend_service.model.order;

import com.example.backend_service.common.RefundStatus;
import com.example.backend_service.model.AbstractEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Refund extends AbstractEntity<Long> {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "customer_bank_info")
    private String customerBankInfo;

    private String reason;

    @Enumerated(EnumType.STRING)
    private RefundStatus status = RefundStatus.PENDING;
}
