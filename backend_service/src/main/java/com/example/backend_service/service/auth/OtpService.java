package com.example.backend_service.service.auth;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.backend_service.dto.request.auth.RegisterRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j(topic = "OTP-SERVICE")
public class OtpService {

    @Data
    @AllArgsConstructor
    public static class OtpData {
        private String otpCode;
        private RegisterRequest registerRequest;
        private LocalDateTime expiryTime;
    }

    // In-memory cache for pending registrations
    private final Map<String, OtpData> otpCache = new ConcurrentHashMap<>();

    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int OTP_LENGTH = 4;
    private static final int EXPIRY_MINUTES = 10;
    private final Random random = new Random();

    public String generateAndCacheOtp(RegisterRequest request) {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }
        
        String code = otp.toString();
        // Overwrites any previous pending registration for this email
        otpCache.put(request.getEmail(), new OtpData(code, request, LocalDateTime.now().plusMinutes(EXPIRY_MINUTES)));
        
        log.info("Generated OTP {} for email {}", code, request.getEmail());
        return code;
    }

    public RegisterRequest verifyOtp(String email, String code) {
        OtpData data = otpCache.get(email);
        
        if (data == null) {
            return null; // Not found
        }
        
        if (data.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpCache.remove(email);
            return null; // Expired
        }
        
        if (data.getOtpCode().equalsIgnoreCase(code)) {
            // Successfully verified matches
            return data.getRegisterRequest();
        }
        
        return null; // Invalid code
    }
    
    public void clearOtp(String email) {
        otpCache.remove(email);
    }
}
