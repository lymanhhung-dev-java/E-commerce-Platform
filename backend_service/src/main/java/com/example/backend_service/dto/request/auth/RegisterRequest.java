package com.example.backend_service.dto.request.auth;

import com.example.backend_service.validator.PasswordMatches;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@PasswordMatches
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 50)
    private String fullName;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100)
    private String password;
    
    @NotBlank(message = "Confirm Password is required")
    @Size(min = 6, max = 100)
    private String confirmPassword;
}
