package com.pos.exception.unit;

import com.pos.exception.UploadValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UploadValidationExceptionTest {

    @Test
    void getters_shouldReturnConstructorValues() {
        byte[] bytes = "err".getBytes();

        UploadValidationException ex = new UploadValidationException(
                "TSV has errors",
                bytes,
                "inventory_upload_errors_x.tsv",
                "text/tab-separated-values"
        );

        assertEquals("TSV has errors", ex.getMessage());
        assertSame(bytes, ex.getFileBytes());
        assertEquals("inventory_upload_errors_x.tsv", ex.getFilename());
        assertEquals("text/tab-separated-values", ex.getContentType());
    }
}
