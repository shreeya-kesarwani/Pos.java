package com.pos.utils;

import com.pos.exception.ApiException;
import com.pos.exception.UploadValidationException;
import com.pos.model.form.InventoryForm;
import com.pos.utils.InventoryTsvParser;
import com.pos.utils.TsvParser;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;

class InventoryTsvParserTest {

    private MockMultipartFile dummyFile() {
        return new MockMultipartFile(
                "file",
                "inv.tsv",
                "text/tab-separated-values",
                "x".getBytes()
        );
    }

    private static void stubTrimS(MockedStatic<TsvParser> mocked) {
        mocked.when(() -> TsvParser.s(any(String[].class), anyInt()))
                .thenAnswer(inv -> {
                    String[] r = inv.getArgument(0);
                    int idx = inv.getArgument(1);
                    if (r == null || idx < 0 || idx >= r.length || r[idx] == null) return "";
                    return r[idx].trim();
                });
    }

    @Test
    void parse_shouldReturnForms_whenValid() throws Exception {
        List<String[]> rows = List.of(
                new String[]{"barcode", "quantity"},
                new String[]{"  b1  ", "10"},
                new String[]{"b2", "0"}
        );

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.read(any())).thenReturn(rows);

            // validateHeader is static void -> do NOT thenReturn(...)
            // Let it run real OR stub it as "do nothing":
            mocked.when(() -> TsvParser.validateHeader(any(String[].class), eq("barcode"), eq("quantity")))
                    .thenAnswer(inv -> null);

            stubTrimS(mocked);

            List<InventoryForm> out = InventoryTsvParser.parse(dummyFile());

            assertEquals(2, out.size());
            assertEquals("b1", out.get(0).getBarcode());
            assertEquals(10, out.get(0).getQuantity());
            assertEquals("b2", out.get(1).getBarcode());
            assertEquals(0, out.get(1).getQuantity());
        }
    }

    @Test
    void parse_shouldThrowUploadValidationException_whenQuantityMissing() throws Exception {
        List<String[]> rows = List.of(
                new String[]{"barcode", "quantity"},
                new String[]{"b1", ""} // quantity missing
        );

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.read(any())).thenReturn(rows);

            mocked.when(() -> TsvParser.validateHeader(any(String[].class), eq("barcode"), eq("quantity")))
                    .thenAnswer(inv -> null);

            stubTrimS(mocked);

            mocked.when(() -> TsvParser.buildErrorTsv(eq(rows), anyList()))
                    .thenReturn("err".getBytes());

            UploadValidationException ex = assertThrows(
                    UploadValidationException.class,
                    () -> InventoryTsvParser.parse(dummyFile())
            );

            assertNotNull(ex.getFileBytes());
            assertTrue(ex.getFileBytes().length > 0);
            assertTrue(ex.getFilename().startsWith("inventory_upload_errors_"));
        }
    }

    @Test
    void parse_shouldThrowUploadValidationException_whenDuplicateBarcode() throws Exception {
        List<String[]> rows = List.of(
                new String[]{"barcode", "quantity"},
                new String[]{"b1", "1"},
                new String[]{" b1 ", "2"} // duplicate after trim
        );

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.read(any())).thenReturn(rows);

            mocked.when(() -> TsvParser.validateHeader(any(String[].class), eq("barcode"), eq("quantity")))
                    .thenAnswer(inv -> null);

            stubTrimS(mocked);

            mocked.when(() -> TsvParser.buildErrorTsv(eq(rows), anyList()))
                    .thenReturn("err".getBytes());

            assertThrows(UploadValidationException.class, () -> InventoryTsvParser.parse(dummyFile()));
        }
    }

    @Test
    void parse_shouldThrowApiException_whenHeaderInvalid() throws Exception {
        List<String[]> rows = List.of(
                new String[]{"wrong", "header"},
                new String[]{"b1", "1"}
        );

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.read(any())).thenReturn(rows);

            mocked.when(() -> TsvParser.validateHeader(any(String[].class), eq("barcode"), eq("quantity")))
                    .thenThrow(new ApiException("bad header"));

            ApiException ex = assertThrows(ApiException.class, () -> InventoryTsvParser.parse(dummyFile()));
            assertEquals("bad header", ex.getMessage());
        }
    }
}
