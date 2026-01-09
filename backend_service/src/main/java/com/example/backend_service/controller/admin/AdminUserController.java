package com.example.backend_service.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.common.UserStatus;
import com.example.backend_service.dto.response.account.UserResponse;
import com.example.backend_service.service.account.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Sort;




@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@Slf4j(topic = "ADMIN-USER-CONTROLLER")
@Tag(name = "Admin User Controller", description = "APIs for admin user management")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminUserController {
    private final UserService userService;

    @Operation(summary = "Get List Users", description = "Lấy danh sách User có lọc nâng cao")
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) Boolean isShopOwner,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    )
    {
        return ResponseEntity.ok(userService.getAllUsers(keyword, status, isShopOwner, pageable));
    }

    @Operation(summary = "Update User Status", description = "Khóa/Mở khóa tài khoản (Truyền status: ACTIVE hoặc INACTIVE)")
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateUserStatus(
            @PathVariable Long id, 
            @RequestParam UserStatus status 
    ) {
        userService.updateUserStatus(id, status);
        
        String message = (status == UserStatus.ACTIVE) ? "Mở khóa thành công" : "Đã khóa tài khoản";
        return ResponseEntity.ok(message);
    }

    
}
