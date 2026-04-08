package com.example.backend_service.service.auth.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.example.backend_service.common.TokenType;
import com.example.backend_service.common.UserStatus;
import com.example.backend_service.dto.request.auth.LoginRequest;
import com.example.backend_service.dto.request.auth.RegisterRequest;
import com.example.backend_service.dto.request.auth.SocialLoginRequest;
import com.example.backend_service.dto.response.auth.GoogleUserInfo;
import com.example.backend_service.dto.response.auth.GoogleTokenResponse;
import com.example.backend_service.dto.response.auth.GoogleUserInfo;
import com.example.backend_service.dto.response.auth.TokenResponse;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.model.auth.Role;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.repository.RoleRepository;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.service.auth.AuthService;
import com.example.backend_service.service.auth.JwtService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import java.util.UUID;
import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j(topic = "AUTH-SERVICE")
public class AuthServiceImpl implements AuthService {

    @Value("${google.client-id}")
    private String googleClientId;
    @Value("${google.client-secret}")
    private String googleClientSecret;
    @Value("${google.redirect-uri}")
    private String googleRedirectUri;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private com.example.backend_service.service.auth.EmailService emailService;
    @Autowired
    private com.example.backend_service.service.auth.OtpService otpService;

    @Override
    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new AppException("Email is already in use");
        }
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new AppException("Username is already in use");
        }

        String otp = otpService.generateAndCacheOtp(registerRequest);
        emailService.sendVerificationCode(registerRequest.getEmail(), otp);
    }

    @Override
    public User verifyRegister(com.example.backend_service.dto.request.auth.VerifyRegisterRequest request) {
        RegisterRequest registerRequest = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (registerRequest == null) {
            throw new AppException("Mã xác thực không hợp lệ hoặc đã hết hạn");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new AppException("Email is already in use");
        }
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new AppException("Username is already in use");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPhoneNumber(registerRequest.getPhone());
        user.setFullName(registerRequest.getFullName());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException("Error: Role USER not found."));
        roles.add(userRole);
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        otpService.clearOtp(request.getEmail());
        log.info("New user registered and verified: {}", savedUser.getUsername());
        return savedUser;
    }

    @Override
    public TokenResponse getAccessToken(LoginRequest loginRequest) {

        List<String> authorities = new ArrayList<>();

        String realUsername = loginRequest.getUsername();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));
            authorities.add(authentication.getAuthorities().toString());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Extract the actual User object from the authentication
            Object principal = authentication.getPrincipal();
            if (principal instanceof com.example.backend_service.model.auth.User) {
                realUsername = ((com.example.backend_service.model.auth.User) principal).getUsername();
            } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                realUsername = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            } else {
                realUsername = principal.toString();
            }
            
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsername());
            throw new AppException("Invalid username or password");
        }
        String accessToken = jwtService.generateAccessToken(realUsername, authorities);
        String refreshToken = jwtService.generateRefreshToken(realUsername, authorities);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public TokenResponse getRefreshToken(String refreshToken) {
        try {
            String username = jwtService.extractUsername(refreshToken, TokenType.REFRESH_TOKEN);
            User user = userRepository.findByUsername(username);
            if (user == null) {
                throw new AppException("User not found");
            }
            List<String> authorities = new ArrayList<>();
            authorities.add(user.getAuthorities().toString());
            String accessToken = jwtService.generateAccessToken(user.getUsername(), authorities);
            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (Exception e) {
            log.error("Failed to refresh token: {}", e.getMessage());
            throw new AppException("Invalid refresh token");
        }

    }

    @Override
    @Transactional
    public TokenResponse googleLogin(SocialLoginRequest req) {
        try {
            GoogleUserInfo googleUser = getGoogleUserInfo(req.getCode());

            User user = userRepository.findFirstByEmail(googleUser.getEmail()).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(googleUser.getEmail());
                newUser.setFullName(googleUser.getName());
                newUser.setUsername(googleUser.getEmail());
                newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                newUser.setAvatarUrl(googleUser.getPicture());
                newUser.setStatus(UserStatus.ACTIVE);

                Role userRole = roleRepository.findByName("USER")
                        .orElseThrow(() -> new AppException("Error: Role USER not found."));
                newUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));
                return userRepository.save(newUser);
            });

            List<String> authorities = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String accessToken = jwtService.generateAccessToken(user.getUsername(), authorities);
            String refreshToken = jwtService.generateRefreshToken(user.getUsername(), authorities);

            log.info("Tokens generated successfully. Access Token length: {}",
                    (accessToken != null ? accessToken.length() : "null"));

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during Google Login: {}", e.getMessage(), e);
            throw new AppException("Lỗi không xác định khi đăng nhập Google: " + e.getMessage());
        }
    }

    private GoogleUserInfo getGoogleUserInfo(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<GoogleTokenResponse> tokenResponse = restTemplate.postForEntity(
                    tokenUrl, request, GoogleTokenResponse.class);

            if (tokenResponse.getBody() == null || tokenResponse.getBody().getAccessToken() == null) {
                log.error("Google Token Response is null or empty");
                throw new AppException("Không thể lấy token từ Google");
            }

            String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setBearerAuth(tokenResponse.getBody().getAccessToken());

            HttpEntity<String> entity = new HttpEntity<>(authHeaders);

            ResponseEntity<GoogleUserInfo> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity,
                    GoogleUserInfo.class);

            if (userInfoResponse.getBody() == null) {
                log.error("Google User Info body is null");
                throw new AppException("Không thể lấy thông tin user từ Google");
            }

            return userInfoResponse.getBody();

        } catch (Exception e) {
            log.error("Google Login Error: {}", e.getMessage(), e);
            throw new AppException("Lỗi đăng nhập Google: " + e.getMessage());
        }
    }

    @Override
    public void forgotPassword(com.example.backend_service.dto.request.auth.ForgotPasswordRequest request) {
        User user = userRepository.findFirstByEmail(request.getEmail())
                .orElseThrow(() -> new AppException("Email không tồn tại trong hệ thống"));
        
        String otp = otpService.generateAndCacheResetPasswordOtp(user.getEmail());
        emailService.sendPasswordResetCode(user.getEmail(), otp);
        log.info("Sent password reset OTP to email: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(com.example.backend_service.dto.request.auth.ResetPasswordRequest request) {
        boolean isValid = otpService.verifyResetPasswordOtp(request.getEmail(), request.getOtp());
        if (!isValid) {
            throw new AppException("Mã xác thực không hợp lệ hoặc đã hết hạn");
        }

        User user = userRepository.findFirstByEmail(request.getEmail())
                .orElseThrow(() -> new AppException("Email không tồn tại trong hệ thống"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpService.clearResetPasswordOtp(request.getEmail());
        log.info("Password successfully reset for email: {}", request.getEmail());
    }
}
