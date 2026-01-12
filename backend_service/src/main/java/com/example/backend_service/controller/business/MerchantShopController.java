package com.example.backend_service.controller.business;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.dto.request.business.RegisterShopRequest;
import com.example.backend_service.dto.request.business.UpdateShopRequest;
import com.example.backend_service.service.business.ShopService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/merchant/shops")
@Slf4j(topic = "MERCHANT-SHOP-CONTROLLER")
@RequiredArgsConstructor
public class MerchantShopController {
    private final ShopService shopService;

    @Operation(summary = "Register Shop", description = "Register a new shop")
    @PostMapping("/register")
    public ResponseEntity<?> registerShop(@RequestBody RegisterShopRequest req) {
        return ResponseEntity.ok(shopService.registerShop(req));

    }

    @Operation(summary = "Get Current Shop", description = "Lấy thông tin Shop của user đang đăng nhập")
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentShop() {
        return ResponseEntity.ok(shopService.getCurrentShop());
    }

    @Operation(summary = "Resubmit Shop Registration", description = "Sửa thông tin và gửi lại yêu cầu duyệt (khi bị từ chối)")
    @PutMapping("/resubmit")
    public ResponseEntity<?> resubmitShop(@RequestBody RegisterShopRequest req) {
        return ResponseEntity.ok(shopService.updateShopRegistration(req));
    }

    @Operation(summary = "Update Shop Info", description = "Cập nhật thông tin cửa hàng (Tên, địa chỉ, logo, mô tả)")
    @PutMapping("/info")
    public ResponseEntity<?> updateShopInfo(@RequestBody UpdateShopRequest req) {
        return ResponseEntity.ok(shopService.updateShopInfo(req));
    }

}
