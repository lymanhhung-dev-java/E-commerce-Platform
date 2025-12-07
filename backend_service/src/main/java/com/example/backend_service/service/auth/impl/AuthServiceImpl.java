package com.example.backend_service.service.auth.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend_service.common.TokenType;
import com.example.backend_service.dto.request.auth.LoginRequest;
import com.example.backend_service.dto.request.auth.RegisterRequest;
import com.example.backend_service.dto.response.auth.TokenResponse;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.model.auth.Role;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.repository.RoleRepository;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.service.auth.AuthService;
import com.example.backend_service.service.auth.JwtService;
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
    @Autowired
    private RoleRepository roleRepository;

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
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException("Error: Role USER not found."));
        roles.add(userRole);
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getUsername());
        return savedUser;
    }

    @Override
    public TokenResponse getAccessToken(LoginRequest loginRequest) {

        List<String> authorities = new ArrayList<>();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));
            log.info("Authorities: {}", authentication.getAuthorities().toString());
            authorities.add(authentication.getAuthorities().toString());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsername());
            throw new AppException("Invalid username or password");
        }
        String accessToken = jwtService.generateAccessToken(loginRequest.getUsername(), authorities);
        String refreshToken = jwtService.generateRefreshToken(loginRequest.getUsername(), authorities);
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

}
