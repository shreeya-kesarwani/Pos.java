package com.pos.unit.utils;

import com.pos.exception.ApiException;
import com.pos.exception.UploadValidationException;
import com.pos.model.form.ProductForm;
import com.pos.utils.ProductTsvParser;
import com.pos.utils.TsvParser;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductTsvParserTest {

    private MockMultipartFile dummyFile() {
        return new MockMultipartFile(
                "file",
                "prod.tsv",
                "text/tab-separated-values",
                "x".getBytes()
        );
    }

    @Test
    void parse_shouldThrowApiException_whenClientIdMissing() {
        assertThrows(ApiException.class, () -> ProductTsvParser.parse(dummyFile(), null));
    }

    @Test
    void parse_shouldThrowApiException_whenFileEmpty() throws Exception {
        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.read(any())).thenReturn(List.of()); // empty
            ApiException ex = assertThrows(ApiException.class, () -> ProductTsvParser.parse(dummyFile(), 1));
            assertEquals("Empty TSV file", ex.getMessage());
        }
    }

    @Test
    void parse_shouldReturnForms_whenValid3ColHeader() throws Exception {
        List<String[]> rows = List.of(
                new String[]{"barcode", "name", "mrp"},
                new String[]{" b1 ", " n1 ", "10.5"},
                new String[]{"b2", "n2", "0"}
        );

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.read(any())).thenReturn(rows);
            mocked.when(() -> TsvParser.s(any(String[].class), anyInt()))
                    .thenAnswer(inv -> {
                        String[] r = inv.getArgument(0);
                        int idx = inv.getArgument(1);
                        if (r == null || idx < 0 || idx >= r.length || r[idx] == null) return "";
                        return r[idx].trim();
                    });
            mocked.when(() -> TsvParser.buildErrorTsv(anyList(), anyList()))
                    .thenReturn("err".getBytes());

            ProductTsvParser.ProductTsvParseResult res = ProductTsvParser.parse(dummyFile(), 99);

            assertEquals(2, res.forms().size());

            ProductForm f1 = res.forms().get(0);
            assertEquals("b1", f1.getBarcode());
            assertEquals("n1", f1.getName());
            assertEquals(10.5, f1.getMrp());
            assertEquals(99, f1.getClientId());
            assertNull(f1.getImageUrl());
        }
    }

    @Test
    void parse_shouldThrowUploadValidationException_whenInvalidHeader() throws Exception {
        List<String[]> rows = List.of(
                new String[]{"barcode", "bad", "mrp"},
                new String[]{"b1", "n1", "10"}
        );

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.read(any())).thenReturn(rows);
            mocked.when(() -> TsvParser.buildErrorTsv(eq(rows), anyList()))
                    .thenReturn("err".getBytes());

            assertThrows(UploadValidationException.class, () -> ProductTsvParser.parse(dummyFile(), 1));
        }
    }

    @Test
    void parse_shouldThrowUploadValidationException_whenDuplicateBarcode() throws Exception {
        List<String[]> rows = List.of(
                new String[]{"barcode", "name", "mrp"},
                new String[]{"b1", "n1", "10"},
                new String[]{" b1 ", "n2", "12"}
        );

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.read(any())).thenReturn(rows);
            mocked.when(() -> TsvParser.s(any(String[].class), anyInt()))
                    .thenAnswer(inv -> {
                        String[] r = inv.getArgument(0);
                        int idx = inv.getArgument(1);
                        if (r == null || idx < 0 || idx >= r.length || r[idx] == null) return "";
                        return r[idx].trim();
                    });
            mocked.when(() -> TsvParser.buildErrorTsv(eq(rows), anyList()))
                    .thenReturn("err".getBytes());

            assertThrows(UploadValidationException.class, () -> ProductTsvParser.parse(dummyFile(), 1));
        }
    }

    @Test
    void parse_shouldThrowUploadValidationException_whenMrpInvalid() throws Exception {
        List<String[]> rows = List.of(
                new String[]{"barcode", "name", "mrp", "imageurl"},
                new String[]{"b1", "n1", "abc", "  "} // invalid mrp, blank image
        );

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.read(any())).thenReturn(rows);
            mocked.when(() -> TsvParser.s(any(String[].class), anyInt()))
                    .thenAnswer(inv -> {
                        String[] r = inv.getArgument(0);
                        int idx = inv.getArgument(1);
                        if (r == null || idx < 0 || idx >= r.length || r[idx] == null) return "";
                        return r[idx].trim();
                    });
            mocked.when(() -> TsvParser.buildErrorTsv(eq(rows), anyList()))
                    .thenReturn("err".getBytes());

            assertThrows(UploadValidationException.class, () -> ProductTsvParser.parse(dummyFile(), 1));
        }
    }
}
