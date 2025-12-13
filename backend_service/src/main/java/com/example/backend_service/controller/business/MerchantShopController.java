package com.example.backend_service.controller.business;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.dto.request.business.RegisterShopRequest;
import com.example.backend_service.service.business.ShopService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/merchant/shops")
@Slf4j(topic = "MERCHANT-SHOP-CONTROLLER")
@RequiredArgsConstructor
public class MerchantShopController {
    private final ShopService shopService;

    @PostMapping("/register")
    public ResponseEntity<?> registerShop(@RequestBody RegisterShopRequest req) {
        return ResponseEntity.ok(shopService.registerShop(req));
        
    }
}
