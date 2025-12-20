package com.example.backend_service.dto.request.checkout;

import java.util.List;

import lombok.Data;

@Data
public class CheckoutRequest {
    private List<Item> items;
    private String shippingAddress;
    private String shippingPhone;

    @Data
    public static class Item {
        private Long productId;
        private Integer quantity;
    }
}
