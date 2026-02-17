package com.pos.unit.utils;

import com.pos.exception.ApiException;
import com.pos.exception.UploadValidationException;
import com.pos.utils.TsvParser;
import com.pos.utils.TsvUploadUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TsvUploadUtilTest {

    @Test
    void parseOrThrow_shouldReturnList_whenAllRowsValid() throws Exception {
        List<String[]> rows = List.of(
                new String[]{"h1", "h2"},
                new String[]{"a", "1"},
                new String[]{"b", "2"}
        );

        AtomicInteger calls = new AtomicInteger(0);

        List<String> out = TsvUploadUtil.parseOrThrow(
                rows,
                "any_prefix",
                (row, lineNo) -> {
                    // ensure correct file line number mapping
                    calls.incrementAndGet();
                    if (lineNo == 2) {
                        assertArrayEquals(new String[]{"a", "1"}, row);
                    } else if (lineNo == 3) {
                        assertArrayEquals(new String[]{"b", "2"}, row);
                    } else {
                        fail("Unexpected lineNo: " + lineNo);
                    }
                    return row[0] + ":" + row[1];
                }
        );

        assertEquals(2, out.size());
        assertEquals("a:1", out.get(0));
        assertEquals("b:2", out.get(1));
        assertEquals(2, calls.get());
    }

    @Test
    void parseOrThrow_shouldThrowUploadValidationException_whenMapperThrowsApiException() throws Exception {
        List<String[]> rows = List.of(
                new String[]{"h1", "h2"},
                new String[]{"a", "1"},
                new String[]{"b", "2"}
        );

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.buildErrorTsv(eq(rows), anyList()))
                    .thenReturn("error-file".getBytes());

            UploadValidationException ex = assertThrows(
                    UploadValidationException.class,
                    () -> TsvUploadUtil.parseOrThrow(
                            rows,
                            "tsv_errors",
                            (row, lineNo) -> {
                                if (lineNo == 3) throw new ApiException("boom");
                                return row[0];
                            }
                    )
            );

            assertEquals("TSV has errors", ex.getMessage());
            assertNotNull(ex.getFileBytes());
            assertTrue(ex.getFileBytes().length > 0);
            assertEquals("text/tab-separated-values", ex.getContentType());
            assertTrue(ex.getFilename().startsWith("tsv_errors_"));
            assertTrue(ex.getFilename().endsWith(".tsv"));
        }
    }

    @Test
    void parseOrThrow_shouldThrowUploadValidationException_whenMapperThrowsRuntimeException() throws Exception {
        List<String[]> rows = List.of(
                new String[]{"h1", "h2"},
                new String[]{"a", "1"}
        );

        try (MockedStatic<TsvParser> mocked = mockStatic(TsvParser.class)) {
            mocked.when(() -> TsvParser.buildErrorTsv(eq(rows), anyList()))
                    .thenReturn("error-file".getBytes());

            UploadValidationException ex = assertThrows(
                    UploadValidationException.class,
                    () -> TsvUploadUtil.parseOrThrow(
                            rows,
                            "runtime_err",
                            (row, lineNo) -> { throw new RuntimeException("kaboom"); }
                    )
            );

            assertEquals("TSV has errors", ex.getMessage());
            assertNotNull(ex.getFileBytes());
            assertTrue(ex.getFilename().startsWith("runtime_err_"));
        }
    }
}
