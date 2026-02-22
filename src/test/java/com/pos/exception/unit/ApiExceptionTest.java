package com.pos.exception.unit;

import com.pos.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionTest {

    @Test
    void ctor_shouldSetMessage() {
        ApiException ex = new ApiException("boom");
        assertEquals("boom", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void ctor_shouldSetMessageAndCause() {
        RuntimeException cause = new RuntimeException("root");
        ApiException ex = new ApiException("boom", cause);

        assertEquals("boom", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}
