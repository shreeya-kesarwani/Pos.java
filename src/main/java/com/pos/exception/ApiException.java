package com.pos.exception;

// Business / validation exception
// Will be handled by GlobalExceptionHandler
public class ApiException extends RuntimeException{

    public ApiException(String message) {
        super(message);
    }
}
