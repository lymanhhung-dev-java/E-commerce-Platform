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
    
    // In-memory cache for password resets (only stores OTP and Expiry because email is the key)
    private final Map<String, OtpData> resetPasswordOtpCache = new ConcurrentHashMap<>();

    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int OTP_LENGTH = 4;
    private static final int EXPIRY_MINUTES = 10;
    private final Random random = new Random();

    public String generateAndCacheOtp(RegisterRequest request) {
        String code = generateRandomCode();
        otpCache.put(request.getEmail(), new OtpData(code, request, LocalDateTime.now().plusMinutes(EXPIRY_MINUTES)));
        log.info("Generated OTP {} for email {}", code, request.getEmail());
        return code;
    }

    public RegisterRequest verifyOtp(String email, String code) {
        OtpData data = otpCache.get(email);
        if (data == null) return null;
        if (data.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpCache.remove(email);
            return null;
        }
        if (data.getOtpCode().equalsIgnoreCase(code)) {
            return data.getRegisterRequest();
        }
        return null;
    }
    
    public void clearOtp(String email) {
        otpCache.remove(email);
    }

    // ==== RESET PASSWORD OTP LOGIC ====

    public String generateAndCacheResetPasswordOtp(String email) {
        String code = generateRandomCode();
        resetPasswordOtpCache.put(email, new OtpData(code, null, LocalDateTime.now().plusMinutes(EXPIRY_MINUTES)));
        log.info("Generated Password Reset OTP {} for email {}", code, email);
        return code;
    }

    public boolean verifyResetPasswordOtp(String email, String code) {
        OtpData data = resetPasswordOtpCache.get(email);
        if (data == null) return false;
        if (data.getExpiryTime().isBefore(LocalDateTime.now())) {
            resetPasswordOtpCache.remove(email);
            return false;
        }
        return data.getOtpCode().equalsIgnoreCase(code);
    }

    public void clearResetPasswordOtp(String email) {
        resetPasswordOtpCache.remove(email);
    }

    private String generateRandomCode() {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }
        return otp.toString();
    }
}
