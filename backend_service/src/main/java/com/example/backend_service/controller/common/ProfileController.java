package com.example.backend_service.controller.common;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.dto.request.account.ChangePasswordRequest;
import com.example.backend_service.dto.request.account.UpdateProfileRequest;
import com.example.backend_service.dto.response.account.ProfileResponse;
import com.example.backend_service.service.account.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
@Slf4j(topic = "PROFILE-CONTROLLER")
@Tag(name = "Profile Controller", description = "APIs for user profile management")
public class ProfileController {
    
    private final UserService userService;

    @Operation(summary = "Get My Profile", description = "Lấy thông tin chi tiết của người dùng đang đăng nhập")
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(){
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Request get profile for user: {}", currentUsername);
        return ResponseEntity.ok(userService.getMyProfile(currentUsername));
    }

    @Operation(summary = "Update Profile", description = "Cập nhật tên, số điện thoại, avatar url")
    @PutMapping("/update")
    public ResponseEntity<ProfileResponse> updateProfile(@RequestBody UpdateProfileRequest req) {
         String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
         log.info("Request update profile for user: {}", currentUsername);
         return ResponseEntity.ok(userService.updateProfile(currentUsername, req));
    }

    @Operation(summary = "Change Password", description = "Đổi mật khẩu (Cần mật khẩu cũ)")
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody @Valid ChangePasswordRequest req){
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Request change password for user: {}", currentUsername);
        userService.changePassword(currentUsername, req);
        return ResponseEntity.ok("oke");
    }
}
