package com.pos.dto;

import com.pos.service.ApiException;
import java.lang.reflect.Field;

public abstract class AbstractDto {

    protected String normalize(String s) {
        if (s == null) {
            return null;
        }
        return s.trim();
    }

    // "check valid using reflections" - Logic to ensure no @NotBlank fields are null/empty
    protected void validateForm(Object form) throws ApiException {
        for (Field field : form.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(form);
                if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                    throw new ApiException("Field " + field.getName() + " cannot be empty");
                }
            } catch (IllegalAccessException e) {
                throw new ApiException("Validation error");
            }
        }
    }

    public void validatePositive(Integer value, String message) throws ApiException { // Changed to public
        if (value == null || value < 0) {
            throw new ApiException(message + " cannot be negative");
        }
    }
}