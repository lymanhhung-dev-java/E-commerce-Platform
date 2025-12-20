package com.example.backend_service.exception;

public class OutOfStockException extends AppException {
    public OutOfStockException(String message) {
        super(message);
    }
}
