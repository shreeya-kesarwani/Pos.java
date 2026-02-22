package com.pos.utils;

import com.pos.utils.InvoicePathUtil;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class InvoicePathUtilTest {

    @Test
    void invoiceFileName_shouldReturnCorrectFormat() {
        String name = InvoicePathUtil.invoiceFileName(5);
        assertTrue(name.contains("5"));
        assertTrue(name.endsWith(".pdf"));
    }

    @Test
    void tryReadInvoiceBytes_shouldReturnNull_whenFileMissing() throws Exception {
        byte[] bytes = InvoicePathUtil.tryReadInvoiceBytes("/tmp/non_existing.pdf", 10);
        assertNull(bytes);
    }

    @Test
    void tryReadInvoiceBytes_shouldReturnBytes_whenFileExists() throws Exception {
        Path dir = Files.createTempDirectory("inv-dir-");
        int orderId = 1;

        InvoicePathUtil.saveInvoiceBytes(dir.toString(), orderId, "abc".getBytes());

        byte[] bytes = InvoicePathUtil.tryReadInvoiceBytes(dir.toString(), orderId);

        assertNotNull(bytes);
        assertEquals(3, bytes.length);
    }

}
