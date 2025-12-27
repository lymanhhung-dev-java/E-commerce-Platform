package com.example.backend_service.dto.request.order;

import java.util.List;

import lombok.Data;

@Data
public class CheckoutRequest {
    private List<Item> items;
    private String shippingAddress;
    private String shippingPhone;
    private String  PaymentMethod = "COD";
    private String note;

    @Data
    public static class Item {
        private Long productId;
        private Integer quantity;
    }
}
