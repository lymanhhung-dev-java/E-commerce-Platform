package com.example.backend_service.model.business;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

import com.example.backend_service.common.WithdrawalStatus;
import com.example.backend_service.model.AbstractEntity;

@Entity
@Table(name = "withdrawals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Withdrawal  extends AbstractEntity<Long>   {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

    private BigDecimal amount; 

    private String bankName;      
    private String accountNumber; 
    private String accountName;   
    @Enumerated(EnumType.STRING)
    private WithdrawalStatus status; 
}
