package com.pos.controller;

import com.pos.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Business Errors
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApiException(
            ApiException exception,
            HttpServletRequest request) {

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", exception.getMessage()));
    }

    // 2. Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String msg = "Validation failed";
        if (exception.getBindingResult().getFieldError() != null) {
            msg = exception.getBindingResult()
                    .getFieldErrors()
                    .get(0)
                    .getDefaultMessage();
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", msg));
    }

    // 3. Invoice Service Down (8081)
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<?> handleNetworkError(
            ResourceAccessException exception,
            HttpServletRequest request) {

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "message",
                        "The Invoice Service is unreachable. Ensure it is running on port 8081."
                ));
    }

    // 4. Unknown Errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(
            Exception exception,
            HttpServletRequest request) {

        exception.printStackTrace();

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "An unexpected system error occurred."));
    }

    // Helper
    private boolean expectsPdf(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/pdf");
    }
}
