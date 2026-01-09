package com.example.backend_service.service.business;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.backend_service.common.ShopStatus;
import com.example.backend_service.dto.request.business.RegisterShopRequest;
import com.example.backend_service.dto.response.business.ShopResponse;
import com.example.backend_service.model.business.Shop;

public interface ShopService {
    Shop registerShop(RegisterShopRequest req);
    void approveShope(Long id, Boolean isApproved);
    Page<ShopResponse> getPendingShopRequests(Pageable pageable);
    ShopResponse getCurrentShop();
    Page<ShopResponse> getShopsForAdmin(String keyword, ShopStatus status, Pageable pageable);
    void banShop(Long shopId);

    Shop updateShopRegistration( RegisterShopRequest req);
}
