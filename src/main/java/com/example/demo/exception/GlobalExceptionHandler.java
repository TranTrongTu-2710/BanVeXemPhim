package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bắt các lỗi ResponseStatusException được ném ra từ các service.
     * Đây là loại lỗi phổ biến nhất mà chúng ta đã chủ động ném ra.
     * Ví dụ: new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatusCode().value())
                .error(HttpStatus.valueOf(ex.getStatusCode().value()).getReasonPhrase())
                .message(ex.getReason())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    /**
     * Bắt các lỗi validation từ các DTO (ví dụ: @NotBlank, @NotNull).
     * Khi một DTO trong @RequestBody không hợp lệ, Spring sẽ ném ra MethodArgumentNotValidException.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Bắt các lỗi chung, không xác định (lỗi 500 Internal Server Error).
     * Đây là một "chốt chặn" cuối cùng để đảm bảo không có lỗi nào làm sập hệ thống
     * hoặc trả về một response không phải JSON.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        // Ghi log lỗi ở đây để debug
        ex.printStackTrace();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred: " + ex.getMessage())
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
