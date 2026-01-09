package com.example.backend_service.dto.response.account;

import com.example.backend_service.model.auth.User;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String role; 
    @JsonProperty("isShopOwner")
    private boolean isShopOwner; 


    public static ProfileResponse fromUser(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRoles().isEmpty() ? "UNKNOWN" : user.getRoles().iterator().next().getName()) 
                .isShopOwner(user.getShop() != null)
                .build();
    }
}
