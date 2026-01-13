package com.example.backend_service.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
    ORDER_ITEM_NOT_FOUND(404, "Không tìm thấy sản phẩm trong đơn hàng", HttpStatus.NOT_FOUND),
    ORDER_NOT_DELIVERED(400, "Đơn hàng chưa được giao thành công", HttpStatus.BAD_REQUEST),
    REVIEW_ALREADY_EXISTS(400, "Bạn đã đánh giá sản phẩm này rồi", HttpStatus.BAD_REQUEST),
    NOT_YOUR_ORDER(403, "Đơn hàng không thuộc về bạn", HttpStatus.FORBIDDEN),
    UNCATEGORIZED_EXCEPTION(500, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
