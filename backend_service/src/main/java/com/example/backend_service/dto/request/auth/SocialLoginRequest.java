package com.example.backend_service.dto.request.auth;

import lombok.Data;

@Data
public class SocialLoginRequest {
    private String code;
}
