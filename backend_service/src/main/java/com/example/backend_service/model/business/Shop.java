package com.example.backend_service.model.business;

import java.io.Serializable;
import java.math.BigDecimal;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.example.backend_service.common.ShopStatus;
import com.example.backend_service.model.AbstractEntity;
import com.example.backend_service.model.auth.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shops")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Shop extends AbstractEntity<Long> implements Serializable {

    @OneToOne
    @JoinColumn(name = "owner_id", unique = true, nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User owner;

    @Column(name = "shop_name", nullable = false)
    private String shopName;

    private String description;

    @Column(name = "balance", precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "logo_url")
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    private ShopStatus status = ShopStatus.PENDING;

    @Column(name = "address")
    private String address;

    private Double rating = 5.0;
}
