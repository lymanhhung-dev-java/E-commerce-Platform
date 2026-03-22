package com.example.backend_service.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
    ORDER_ITEM_NOT_FOUND(404, "Không tìm thấy sản phẩm trong đơn hàng", HttpStatus.NOT_FOUND),
    ORDER_NOT_DELIVERED(400, "Đơn hàng chưa được giao thành công", HttpStatus.BAD_REQUEST),
    REVIEW_ALREADY_EXISTS(400, "Bạn đã đánh giá sản phẩm này rồi", HttpStatus.BAD_REQUEST),
    NOT_YOUR_ORDER(403, "Đơn hàng không thuộc về bạn", HttpStatus.FORBIDDEN),
    NOT_FOUND(404, "Không tìm thấy dữ liệu", HttpStatus.NOT_FOUND),
    UNAUTHORIZED(403, "Không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),
    ALREADY_EXIST(400, "Dữ liệu đã tồn tại", HttpStatus.BAD_REQUEST),
    BAD_REQUEST(400, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
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
