package com.example.backend_service.dto.response.account;

import java.time.LocalDateTime;
import com.example.backend_service.common.UserStatus;
import com.example.backend_service.model.auth.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private UserStatus status;
    private String role;
    @JsonProperty("isShopOwner")
    private boolean isShopOwner; 
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        String roleName = user.getRoles().isEmpty() ? "UNKNOWN" : user.getRoles().iterator().next().getName();
        
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .role(roleName)
                .isShopOwner(user.getShop() != null) 
                .createdAt(user.getCreatedAt())
                .build();
    }
}