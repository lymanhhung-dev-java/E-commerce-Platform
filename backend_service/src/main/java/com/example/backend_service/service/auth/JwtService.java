package com.example.backend_service.service.auth;

import java.util.List;
import com.example.backend_service.common.TokenType;

public interface JwtService {
    
    String generateAccessToken(String username, List<String> authorities);

    String generateRefreshToken(String username, List<String> authorities);

    String extractUsername(String token, TokenType tokenType);
}
