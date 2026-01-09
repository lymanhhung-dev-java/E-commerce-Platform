package com.example.backend_service.model.auth;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "verification_tokens")
@Data
@NoArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private LocalDateTime expiryDate;

    // Hàm tiện ích để tính ngày hết hạn (Ví dụ: hết hạn sau 24h)
    public void setExpiryDate(int minutes){
        this.expiryDate = LocalDateTime.now().plusMinutes(minutes);
    }
    
    // Constructor tiện lợi
    public VerificationToken(User user, String token) {
        this.user = user;
        this.token = token;
        this.expiryDate = LocalDateTime.now().plusMinutes(15); // Mặc định 15 phút
    }
}
