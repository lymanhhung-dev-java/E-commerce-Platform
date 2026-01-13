package com.example.backend_service.service.product.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend_service.common.OrderStatus;
import com.example.backend_service.dto.request.ReviewRequest;
import com.example.backend_service.dto.response.ReviewResponse;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.exception.ErrorCode;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.model.order.OrderItem;
import com.example.backend_service.model.product.Review;
import com.example.backend_service.repository.OrderItemRepository;
import com.example.backend_service.repository.ReviewRepository;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.service.product.ReviewService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username);

        if (currentUser == null) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        List<OrderItem> eligibleItems = orderItemRepository
                .findDeliveredItemsByProductAndUser(request.getProductId(), currentUser.getId(),
                        OrderStatus.DELIVERED);

        if (eligibleItems.isEmpty()) {
            throw new AppException(ErrorCode.ORDER_NOT_DELIVERED);
        }

        OrderItem selectedItem = null;
        for (OrderItem item : eligibleItems) {
            if (!reviewRepository.existsByOrderItemId(item.getId())) {
                selectedItem = item;
                break;
            }
        }

        if (selectedItem == null) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = new Review();
        review.setUser(currentUser);
        review.setProduct(selectedItem.getProduct());
        review.setOrderItem(selectedItem);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        review = reviewRepository.save(review);

        return ReviewResponse.builder()
                .id(review.getId())
                .userName(currentUser.getFullName())
                .productName(selectedItem.getProduct().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    @Override
    public Page<ReviewResponse> getReviewsByProduct(Long productId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageable);
        return reviewPage.map(review -> ReviewResponse.builder()
                .id(review.getId())
                .userName(review.getUser().getFullName())
                .productName(review.getProduct().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build());
    }
}
