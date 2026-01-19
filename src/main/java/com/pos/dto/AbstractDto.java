package com.pos.dto;

import com.pos.service.ApiException;

public abstract class AbstractDto {

    // Normalizes strings: removes leading/trailing spaces and handles nulls
    protected String normalize(String s) {
        return (s == null) ? "" : s.trim();
    }

    // Common validation logic used across different forms
    protected void validatePositive(Integer value, String fieldName) throws ApiException {
        if (value == null || value < 0) {
            throw new ApiException(fieldName + " must be a positive number");
        }
    }
}