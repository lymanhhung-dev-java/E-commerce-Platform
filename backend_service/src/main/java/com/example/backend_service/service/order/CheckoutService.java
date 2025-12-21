package com.example.backend_service.service.order;

import com.example.backend_service.dto.request.order.CheckoutRequest;

public interface CheckoutService {
    Long checkout(CheckoutRequest request, String username);
}
