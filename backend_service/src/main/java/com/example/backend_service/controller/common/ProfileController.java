package com.example.backend_service.controller.common;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/profile")
@Slf4j(topic = "PROFILE-CONTROLLER")
@Tag(name = "Profile Controller", description = "APIs for user profile management")
public class ProfileController {
    
}
