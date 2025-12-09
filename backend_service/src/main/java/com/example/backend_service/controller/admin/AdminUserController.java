package com.example.backend_service.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.service.account.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
@Slf4j(topic = "ADMIN-USER-CONTROLLER")
@Tag(name = "Admin User Controller", description = "APIs for admin user management")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {
    private final UserService userService;

    
}
