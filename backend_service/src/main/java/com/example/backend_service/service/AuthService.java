package com.example.backend_service.service;

import com.example.backend_service.dto.request.auth.LoginRequest;
import com.example.backend_service.dto.request.auth.RegisterRequest;
import com.example.backend_service.dto.response.auth.TokenResponse;
import com.example.backend_service.model.User;

public interface AuthService {
   User register(RegisterRequest registerRequest);

   TokenResponse getAccessToken(LoginRequest loginRequest);
   
   TokenResponse getRefreshToken(String refreshToken);
} 
