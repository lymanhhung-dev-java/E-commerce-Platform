package com.example.backend_service.service.order;

import java.util.List;

import com.example.backend_service.dto.request.order.CheckoutRequest;
import com.example.backend_service.dto.response.order.PaymentQrResponse;

public interface CheckoutService {
    List<Long> checkout(CheckoutRequest request);
    boolean checkPaymentStatus(Long orderId);
    PaymentQrResponse getPaymentQrUrl(Long orderId);
    void cancelOrderAndRestoreCart(Long orderId);

}
