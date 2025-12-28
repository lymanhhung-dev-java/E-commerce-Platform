package com.example.backend_service.service.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.backend_service.common.UserStatus;
import com.example.backend_service.dto.request.account.ChangePasswordRequest;
import com.example.backend_service.dto.request.account.UpdateProfileRequest;
import com.example.backend_service.dto.response.account.ProfileResponse;
import com.example.backend_service.dto.response.account.UserResponse;

public interface UserService {
    
    ProfileResponse getMyProfile(String currentUsername);

    ProfileResponse updateProfile(String currentUsername, UpdateProfileRequest req);

    void changePassword(String currentUsername, ChangePasswordRequest req);

    Page<UserResponse> getAllUsers(String keyword, UserStatus status, Boolean isShopOwner, Pageable pageable);

    void updateUserStatus(Long userId, UserStatus status);
}
