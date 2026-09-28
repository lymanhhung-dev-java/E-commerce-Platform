package com.example.backend_service.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * Gửi mã OTP xác thực tài khoản qua Gmail SMTP
     *
     * @param recipientEmail email người nhận
     * @param otp mã xác thực
     */
    @Async
    public void sendOtp(String recipientEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromEmail != null && !fromEmail.isBlank()) {
                message.setFrom(fromEmail);
            }
            message.setTo(recipientEmail);
            message.setSubject("Mã xác thực tài khoản");
            message.setText("Xin chào,\n\n"
                    + "Mã xác thực (OTP) của bạn là: " + otp + "\n\n"
                    + "Mã xác thực này có hiệu lực trong vòng 10 phút. "
                    + "Vui lòng không cung cấp mã này cho bất kỳ ai để đảm bảo an toàn cho tài khoản của bạn.\n\n"
                    + "Trân trọng,\n"
                    + "E-Commerce Platform Support");

            mailSender.send(message);
            log.info("OTP sent successfully to email: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", recipientEmail, e.getMessage());
        }
    }

    /**
     * Gửi mã xác nhận đăng ký tài khoản
     */
    @Async
    public void sendVerificationCode(String toEmail, String code) {
        sendOtp(toEmail, code);
    }

    /**
     * Gửi mã xác thực cấp lại mật khẩu
     */
    @Async
    public void sendPasswordResetCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromEmail != null && !fromEmail.isBlank()) {
                message.setFrom(fromEmail);
            }
            message.setTo(toEmail);
            message.setSubject("Mã xác nhận cấp lại mật khẩu (E-commerce Platform)");
            message.setText("Xin chào,\n\n"
                    + "Bạn vừa yêu cầu cấp lại mật khẩu cho tài khoản liên kết với địa chỉ email này.\n"
                    + "Mã xác nhận (OTP) của bạn là: " + code + "\n\n"
                    + "Mã xác thực có hiệu lực trong vòng 10 phút. "
                    + "Vui lòng không cung cấp mã này cho người lạ để tránh rủi ro bảo mật dữ liệu.\n\n"
                    + "Trân trọng,\n"
                    + "E-Commerce Platform Support");

            mailSender.send(message);
            log.info("Password reset code sent to email: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }
}
