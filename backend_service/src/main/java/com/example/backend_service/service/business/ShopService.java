package com.example.backend_service.service.business;

import com.example.backend_service.dto.request.business.RegisterShopRequest;
import com.example.backend_service.model.business.Shop;

public interface ShopService {
    Shop registerShop(RegisterShopRequest req);
    void approveShope(Long id, Boolean isApproved);
}
