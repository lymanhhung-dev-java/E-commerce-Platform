package com.example.backend_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.security.access.AccessDeniedException;
import java.util.Date;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice 
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, Object>> handleAppException(AppException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 400);
        response.put("message", ex.getMessage()); 
        response.put("success", false);
        
        return ResponseEntity.badRequest().body(response); 
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        
        String message = ex.getBindingResult().getFieldError().getDefaultMessage();
        
        response.put("status", 400);
        response.put("message", message);
        response.put("success", false);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnwantedException(Exception ex) {
        ex.printStackTrace(); 
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", 500);
        response.put("message", "Lỗi hệ thống vui lòng thử lại sau"); 
        response.put("success", false);

        return ResponseEntity.internalServerError().body(response);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
    
        String path = request.getDescription(false).replace("uri=", "");

        response.put("timestamp", new Date()); 
        response.put("status", 403);
        response.put("error", HttpStatus.FORBIDDEN.getReasonPhrase()); 
        response.put("message", ex.getMessage()); 
        response.put("path", path); 
        response.put("success", false); 

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

}