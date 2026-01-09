package com.example.backend_service.validator;

import com.example.backend_service.dto.request.auth.RegisterRequest;

public class PasswordMatchesValidator implements jakarta.validation.ConstraintValidator<PasswordMatches, RegisterRequest> {
 @Override
 public boolean isValid(RegisterRequest registerRequest, jakarta.validation.ConstraintValidatorContext context) {
     if (registerRequest == null || registerRequest.getPassword() == null || registerRequest.getConfirmPassword() == null) {
        return false;
     }
     boolean matches = registerRequest.getPassword().equals(registerRequest.getConfirmPassword());
        if (!matches) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Passwords do not match")
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }
        return matches;
 }
    
}
