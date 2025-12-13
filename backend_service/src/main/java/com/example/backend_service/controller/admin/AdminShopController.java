package com.example.backend_service.controller.admin;

import org.springframework.web.bind.annotation.RestController;
import com.example.backend_service.service.business.ShopService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;



@RestController
@RequestMapping("/api/admin/shops")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Slf4j(topic = "ADMIN-SHOP-CONTROLLER")
@Tag(name = "Admin Shop Controller", description = "APIs for admin shop management")
@RequiredArgsConstructor
public class AdminShopController {

    private final ShopService shopService;

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveShop(@PathVariable Long shopId, @RequestParam Boolean isApproved){
        shopService.approveShope(shopId, isApproved);
        return ResponseEntity.ok(isApproved ? "Shop đã được phê duyệt" : "Shop đã bị từ chối");
    }
    

}
