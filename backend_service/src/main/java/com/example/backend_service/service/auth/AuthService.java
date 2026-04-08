package com.example.backend_service.service.auth;

import com.example.backend_service.dto.request.auth.LoginRequest;
import com.example.backend_service.dto.request.auth.RegisterRequest;
import com.example.backend_service.dto.request.auth.SocialLoginRequest;
import com.example.backend_service.dto.response.auth.TokenResponse;
import com.example.backend_service.model.auth.User;

import com.example.backend_service.dto.request.auth.VerifyRegisterRequest;

public interface AuthService {
   void register(RegisterRequest registerRequest);

   User verifyRegister(VerifyRegisterRequest request);

   TokenResponse getAccessToken(LoginRequest loginRequest);
   
   TokenResponse getRefreshToken(String refreshToken);

   TokenResponse googleLogin(SocialLoginRequest request);

   void forgotPassword(com.example.backend_service.dto.request.auth.ForgotPasswordRequest request);

   void resetPassword(com.example.backend_service.dto.request.auth.ResetPasswordRequest request);
}
