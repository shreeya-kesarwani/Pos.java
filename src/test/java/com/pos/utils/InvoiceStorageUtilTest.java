package com.pos.utils;

import com.pos.api.OrderApi;
import com.pos.utils.InvoiceStorageUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvoiceStorageUtilTest {

    @Test
    void storeAndAttach_shouldSaveFileAndCallOrderApi() throws Exception {
        OrderApi orderApi = mock(OrderApi.class);
        byte[] pdf = "data".getBytes();

        Path dir = Files.createTempDirectory("inv-test-");

        InvoiceStorageUtil.storeAndAttach(orderApi, dir.toString(), 1, pdf);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(orderApi).generateInvoice(eq(1), captor.capture());

        String path = captor.getValue();
        assertNotNull(path);

        // file name format is INV-<id>.pdf
        assertTrue(path.endsWith("INV-1.pdf") || path.endsWith(File.separator + "INV-1.pdf"));

        // and file actually exists
        assertTrue(Files.exists(Path.of(path)));
        assertArrayEquals(pdf, Files.readAllBytes(Path.of(path)));
    }
}
