package com.pos.controller;

import com.pos.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handled Business Errors
    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleApiException(ApiException exception) {
        return Map.of("message", exception.getMessage());
    }

    // 2. Spring Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException exception) {
        String msg = "Validation failed";
        if (exception.getBindingResult().getFieldError() != null) {
            msg = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        return Map.of("message", msg != null ? msg : "Invalid input provided");
    }

    // 3. Handle Network/Microservice Failures (If Invoice App 8081 is down)
    @ExceptionHandler(ResourceAccessException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String, String> handleNetworkError(ResourceAccessException exception) {
        return Map.of("message", "The Invoice Service is unreachable. Ensure it is running on port 8081.");
    }

    // 4. Unexpected System Errors
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGeneral(Exception exception) {
        exception.printStackTrace(); // Log for debugging
        return Map.of("message", "An unexpected system error occurred.");
    }
}