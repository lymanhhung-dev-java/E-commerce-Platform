package com.example.backend_service.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrator {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrate() {
        try {
            // Fix truncation error for message_type when adding PRODUCT_INFO (length > 10)
            jdbcTemplate.execute("ALTER TABLE chat_messages MODIFY COLUMN message_type VARCHAR(50)");
        } catch (Exception e) {
            // Ignore if column doesn't exist or other minor issues
        }
    }
}
