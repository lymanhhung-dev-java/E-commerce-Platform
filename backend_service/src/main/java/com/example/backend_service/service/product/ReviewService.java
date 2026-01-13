package com.example.backend_service.service.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.backend_service.dto.request.ReviewRequest;
import com.example.backend_service.dto.response.ReviewResponse;

public interface ReviewService {
    ReviewResponse createReview(ReviewRequest request);

    Page<ReviewResponse> getReviewsByProduct(Long productId, Pageable pageable);
}
