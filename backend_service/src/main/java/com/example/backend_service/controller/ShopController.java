package com.example.backend_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.service.business.ShopService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/shops")
@Slf4j(topic = "SHOP-CONTROLLER")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @Operation(summary = "Get Shop Details", description = "Get details of a specific shop")
    @GetMapping("/{id}")
    public ResponseEntity<?> getShopById(@PathVariable Long id) {
        return ResponseEntity.ok(shopService.getShopById(id));
    }
}
