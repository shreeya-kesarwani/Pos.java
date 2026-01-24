package com.pos.dto;

import com.pos.exception.ApiException;
import java.lang.reflect.Field;

public abstract class AbstractDto {

    protected <T> void normalize(T form) throws ApiException {
        if (form == null) return;

        for (Field field : form.getClass().getDeclaredFields()) {
            if (field.getType().equals(String.class)) {
                try {
                    field.setAccessible(true);
                    String value = (String) field.get(form);
                    if (value != null) {
                        field.set(form, value.trim());
                    }
                } catch (IllegalAccessException e) {
                    throw new ApiException("Error during data normalization");
                }
            }
        }
    }

    protected void validateForm(Object form) throws ApiException {
        if (form == null) throw new ApiException("Form cannot be null");

        for (Field field : form.getClass().getDeclaredFields()) {
//            if (field.getName().equals("imageUrl")) {
//                continue;
//            }

            field.setAccessible(true);
            try {
                Object value = field.get(form);
                if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                    throw new ApiException(String.format("Field [%s] cannot be empty", field.getName()));
                }
            } catch (IllegalAccessException e) {
                throw new ApiException("Validation error occurred");
            }
        }
    }

    protected String normalize(String s) {
        return (s == null) ? null : s.trim().toLowerCase();
    }

    protected void validatePositive(Double value, String fieldName) throws ApiException {
        if (value == null || value <= 0) {
            throw new ApiException(String.format("%s must be a positive number", fieldName));
        }
    }
}