package com.example.backend_service.service.auth;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import com.example.backend_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserServiceDetail {
    private final UserRepository userRepository;
    public UserDetailsService userDetailsService() {
        return identifier -> {
            com.example.backend_service.model.auth.User user = userRepository.findByUsername(identifier);
            if (user == null) {
                user = userRepository.findFirstByEmail(identifier).orElse(null);
            }
            if (user == null) {
                throw new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found: " + identifier);
            }
            return user;
        };
    }
}
