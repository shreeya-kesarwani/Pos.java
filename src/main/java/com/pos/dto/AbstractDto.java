package com.pos.dto;

import com.pos.exception.ApiException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.Set;

import static com.pos.model.constants.ErrorMessages.ERROR_DURING_NORMALIZATION; // <-- add in enum

public abstract class AbstractDto {

    @Autowired
    private Validator validator;

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
                    throw new ApiException(ERROR_DURING_NORMALIZATION.value());
                }
            }
        }
    }

    protected String normalize(String string) {
        return (string == null || string.isEmpty()) ? null : string.trim();
    }

    protected <T> void validateForm(T obj) throws ApiException {
        Set<ConstraintViolation<T>> violations = validator.validate(obj);
        if (!violations.isEmpty()) {
            throw new ApiException(violations.iterator().next().getMessage());
        }
    }

}
