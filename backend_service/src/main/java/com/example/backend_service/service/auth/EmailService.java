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
        // CỰC KỲ QUAN TRỌNG: Phải là email bạn dùng đăng ký tài khoản Brevo
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
}
