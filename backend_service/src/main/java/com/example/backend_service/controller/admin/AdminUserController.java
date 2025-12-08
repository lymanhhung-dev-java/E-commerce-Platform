package com.example.backend_service.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/admin/users")
@Slf4j(topic = "ADMIN-USER-CONTROLLER")
@Tag(name = "Admin User Controller", description = "APIs for admin user management")
@PreAuthorize("hashAuthority('ADMIN')")
public class AdminUserController {

    @Autowired
    

    @GetMapping("path")
    public String getMethodName(@RequestParam String param) {
        return new String();
    }
    
    
}
