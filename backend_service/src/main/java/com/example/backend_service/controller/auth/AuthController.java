package com.example.backend_service.controller.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.backend_service.dto.request.auth.LoginRequest;
import com.example.backend_service.dto.request.auth.RegisterRequest;
import com.example.backend_service.dto.request.auth.SocialLoginRequest;
import com.example.backend_service.dto.request.auth.VerifyRegisterRequest;
import com.example.backend_service.dto.response.auth.TokenResponse;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
@Slf4j(topic = "AUTH-CONTROLLER")
@Tag(name = "Auth Controller", description = "APIs for user authentication and registration")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Operation(summary = "Register", description = "API to register a new user using OTP email verification")
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.ok("Mã xác thực đã được gửi đến email của bạn.");
    }

    @Operation(summary = "Verify Register", description = "API to verify OTP and complete registration")
    @PostMapping("/verify-register")
    public ResponseEntity<Long> verifyRegister(@RequestBody @Valid VerifyRegisterRequest req) {
        User registeredUser = authService.verifyRegister(req);
        return ResponseEntity.ok(registeredUser.getId());
    }

    @Operation(summary = "Access Token", description = "API to obtain an access token using user credentials")
    @PostMapping("/acces-token")
    public TokenResponse getAccessToken(@RequestBody LoginRequest req) {
        log.info("Access token requested");
        return authService.getAccessToken(req);

    }

    @Operation(summary = "Refresh Token", description = "API to obtain a new access token using a refresh token")
    @PostMapping("/refresh-token")
    public TokenResponse getRefreshToken(@RequestBody String refreshToken) {
        log.info("refresh token requested");
        return authService.getRefreshToken(refreshToken);
    }

    @Operation(summary = "Google Login", description = "API to login with Google")
    @PostMapping("/google")
    public ResponseEntity<TokenResponse> googleLogin(@RequestBody SocialLoginRequest req) {
        log.info("Google login requested");
        TokenResponse tokenResponse = authService.googleLogin(req);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(summary = "Forgot Password - Send Code", description = "Send OTP to email for password reset")
    @PostMapping("/forgot-password/send-code")
    public ResponseEntity<String> sendForgotPasswordCode(@RequestBody @Valid com.example.backend_service.dto.request.auth.ForgotPasswordRequest req) {
        log.info("Forgot password send code requested for email: {}", req.getEmail());
        authService.forgotPassword(req);
        return ResponseEntity.ok("Mã xác thực đã được gửi đến email của bạn.");
    }

    @Operation(summary = "Forgot Password - Reset", description = "Verify OTP and reset password")
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid com.example.backend_service.dto.request.auth.ResetPasswordRequest req) {
        log.info("Reset password requested for email: {}", req.getEmail());
        authService.resetPassword(req);
        return ResponseEntity.ok("Mật khẩu đã được thay đổi thành công. Vui lòng đăng nhập lại.");
    }
}
