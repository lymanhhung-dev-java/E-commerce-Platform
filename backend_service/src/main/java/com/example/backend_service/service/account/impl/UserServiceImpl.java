package com.example.backend_service.service.account.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend_service.common.UserStatus;
import com.example.backend_service.dto.request.account.ChangePasswordRequest;
import com.example.backend_service.dto.request.account.UpdateProfileRequest;
import com.example.backend_service.dto.response.account.ProfileResponse;
import com.example.backend_service.dto.response.account.UserResponse;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.repository.specification.UserSpecification;
import com.example.backend_service.service.account.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "USER-SERVICE")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ProfileResponse getMyProfile(String currentUsername) {
        User user = getUserByUsername(currentUsername);
        return ProfileResponse.fromUser(user);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(String currentUsername, UpdateProfileRequest req) {
        User user = getUserByUsername(currentUsername);

        if (req.getFullName() != null && !req.getFullName().isBlank()) {
            user.setFullName(req.getFullName());
        }
        if (req.getPhoneNumber() != null && !req.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(req.getPhoneNumber());
        }
        if (req.getAvatarUrl() != null && !req.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(req.getAvatarUrl());
        }

        User updatedUser = userRepository.save(user);
        return ProfileResponse.fromUser(updatedUser);
    }

    @Override
    @Transactional

    public void changePassword(String currentUsername, ChangePasswordRequest req) {
        User user = getUserByUsername(currentUsername);

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new AppException("Confirmation password does not match");
        }

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new AppException("Incorrect old password");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    private User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new AppException("User not found");
        }
        return user;
    }

    @Override
    public Page<UserResponse> getAllUsers(String keyword, UserStatus status, Boolean isShopOwner, Pageable pageable) {

        Specification<User> spec = Specification.where(UserSpecification.hasKeyword(keyword))
                .and(UserSpecification.hasStatus(status))
                .and(UserSpecification.isShopOwner(isShopOwner));

        return userRepository.findAll(spec, pageable)
                .map(UserResponse::fromEntity);
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("Người dùng không tồn tại"));
        user.setStatus(status);
        userRepository.save(user);
        
        log.info("Đã cập nhật trạng thái user ID {} thành {}", userId, status);
    }

}

