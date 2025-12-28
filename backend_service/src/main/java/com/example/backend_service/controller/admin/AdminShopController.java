package com.example.backend_service.controller.admin;

import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.common.ShopStatus;
import com.example.backend_service.dto.response.business.ShopResponse;
import com.example.backend_service.service.business.ShopService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.data.domain.Sort;



@RestController
@RequestMapping("/api/admin/shops")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Slf4j(topic = "ADMIN-SHOP-CONTROLLER")
@Tag(name = "Admin Shop Controller", description = "APIs for admin shop management")
@RequiredArgsConstructor
public class AdminShopController {

    private final ShopService shopService;

    @Operation(summary = "Approve Shop", description = "Approve or reject a shop")
    @PutMapping("/{shopId}/approve")
    public ResponseEntity<?> approveShop(@PathVariable Long shopId, @RequestParam Boolean isApproved){
        shopService.approveShope(shopId, isApproved);
        return ResponseEntity.ok(isApproved ? "Shop đã được phê duyệt" : "Shop đã bị từ chối");
    }

    @Operation(summary = "Get Pending Shop Requests", description = "Lấy danh sách các yêu cầu mở Shop đang chờ duyệt")
    @GetMapping("/requests")
    public ResponseEntity<Page<ShopResponse>> getPendingRequests(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(shopService.getPendingShopRequests(pageable));
    }

    @Operation(summary = "Ban Shop", description = "Khóa shop do vi phạm (Không cho sửa lại)")
    @PutMapping("/{id}/ban") 
    public ResponseEntity<String> banShop(@PathVariable Long id) {
        shopService.banShop(id);
        return ResponseEntity.ok("Đã khóa Shop thành công");
    }

    @Operation(summary = "Get All Shops", description = "Lấy danh sách shop (có search & filter)")
    @GetMapping
    public ResponseEntity<Page<ShopResponse>> getAllShops(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ShopStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(shopService.getShopsForAdmin(keyword, status, pageable));
    }

    

}
