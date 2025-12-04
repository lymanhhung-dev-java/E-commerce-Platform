package com.example.backend_service.service;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

import com.example.backend_service.common.TokenType;

public interface JwtService {
    
    String generateAccessToken(long userId,String username, Collection<? extends GrantedAuthority> authorities);

    String generateRefreshToken(long userId,String username, Collection<? extends GrantedAuthority> authorities);

    String extractUsername(String token, TokenType tokenType);
}
