package com.example.backend_service.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j(topic = "EMAIL-SERVICE")
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("ignuh204@gmail.com"); 
            message.setTo(toEmail);
            message.setSubject("Mã xác nhận đăng ký tài khoản");
            message.setText("Mã của bạn là: " + code);
            
            mailSender.send(message);
            log.info("Verification code sent to email: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("ignuh204@gmail.com"); 
            message.setTo(toEmail);
            message.setSubject("Mã xác nhận cấp lại mật khẩu (E-commerce Platform)");
            message.setText("Bạn vừa yêu cầu cấp lại mật khẩu cho tài khoản liên kết với thẻ Email này.\nMã xác nhận nhận diện của bạn là: " + code + "\n\nVui lòng không cung cấp mã này cho người lạ để tránh rủi ro bảo mật dữ liệu.\n\nTrân trọng.");
            
            mailSender.send(message);
            log.info("Password reset code sent to email: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }
}
