package com.example.backend_service.service.impl;

import javax.naming.AuthenticationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend_service.dto.request.auth.LoginRequest;
import com.example.backend_service.dto.request.auth.RegisterRequest;
import com.example.backend_service.dto.response.auth.TokenResponse;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.model.User;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.service.AuthService;
import com.example.backend_service.service.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j(topic = "AUTH-SERVICE")
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;

    @Override
    public User register(RegisterRequest registerRequest) {
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
        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getUsername());
        return savedUser;
    }

    @Override
    public TokenResponse getAccessToken(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()));

        try {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsername());
            throw new AppException("Invalid username or password");
        }

        var user = userRepository.findByUsername(loginRequest.getUsername());

        if (user == null) {
            log.error("User not found: {}", loginRequest.getUsername());
            throw new AppException("User not found");
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), loginRequest.getUsername(), null);
        String refreshToken = jwtService.generateRefreshToken(user.getId(), loginRequest.getUsername(), null);
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public TokenResponse getRefreshToken(String refreshToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRefreshToken'");
    }

}
