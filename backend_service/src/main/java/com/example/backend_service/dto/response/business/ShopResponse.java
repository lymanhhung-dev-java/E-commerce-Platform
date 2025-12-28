package com.example.backend_service.dto.response.business;

import java.time.LocalDateTime;
import com.example.backend_service.common.ShopStatus;
import com.example.backend_service.model.business.Shop;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShopResponse {
    private Long id;
    private String shopName;
    private String description;
    private String logoUrl;
    private String address;
    private ShopStatus status;
    private String ownerName;     
    private String ownerEmail;    
    private LocalDateTime createdAt;

    public static ShopResponse fromEntity(Shop shop) {
        return ShopResponse.builder()
                .id(shop.getId())
                .shopName(shop.getShopName())
                .description(shop.getDescription())
                .logoUrl(shop.getLogoUrl())
                .address(shop.getAddress())
                .status(shop.getStatus())
                .ownerName(shop.getOwner().getFullName()) 
                .ownerEmail(shop.getOwner().getEmail())
                .createdAt(shop.getCreatedAt())
                .build();
    }
}