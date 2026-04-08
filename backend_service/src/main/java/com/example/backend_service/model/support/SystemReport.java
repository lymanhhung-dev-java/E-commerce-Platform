package com.example.backend_service.model.support;

import com.example.backend_service.common.ComplaintStatus;
import com.example.backend_service.model.AbstractEntity;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.order.Order;
import com.example.backend_service.model.business.Shop;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "system_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemReport extends AbstractEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = true)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = true)
    private Order order;

    @Column(name = "reason_type", nullable = false)
    private String reasonType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintStatus status = ComplaintStatus.PENDING;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;
}
