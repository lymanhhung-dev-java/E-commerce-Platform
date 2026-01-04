package com.example.backend_service.service.order;

import java.util.List;

import com.example.backend_service.dto.request.order.CheckoutRequest;

public interface CheckoutService {
    List<Long> checkout(CheckoutRequest request);
    boolean checkPaymentStatus(Long orderId);
     String getPaymentQrUrl(Long orderId);
     void cancelOrderAndRestoreCart(Long orderId);

}
