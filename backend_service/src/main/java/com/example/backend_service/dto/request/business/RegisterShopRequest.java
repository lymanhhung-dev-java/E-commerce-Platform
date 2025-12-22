package com.example.backend_service.dto.request.business;

import lombok.Data;

@Data
public class RegisterShopRequest {
    private String ShopName;
    private String description;
    private String logoUrl;
    private String address;
}
