package com.example.backend_service.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Old password must not be empty")
    private String oldPassword;

    @NotBlank(message = "New password must not be empty")
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;

    @NotBlank(message = "Confirm password must not be empty")
    private String confirmPassword;
}
