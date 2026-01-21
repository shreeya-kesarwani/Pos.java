package com.pos.controller;

import com.pos.service.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
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

    // 3. Unexpected System Errors
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGeneral(Exception exception) {
        // Log the actual exception here for developers to see in console
        exception.printStackTrace();

        // Return a generic message so you don't leak DB details to users
        return Map.of("message", "An unexpected system error occurred. Please contact support.");
    }
}