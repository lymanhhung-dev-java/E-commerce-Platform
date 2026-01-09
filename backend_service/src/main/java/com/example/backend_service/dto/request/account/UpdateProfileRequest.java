package com.example.backend_service.dto.request.account;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
}
