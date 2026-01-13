package com.example.backend_service.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private String userName;
    private String productName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
