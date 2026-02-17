package com.pos.unit.utils;

import com.pos.utils.TsvParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TsvParserTest {

    @Test
    void read_shouldParseRows() throws Exception {
        String data = "a\tb\n1\t2";
        List<String[]> rows = TsvParser.read(new ByteArrayInputStream(data.getBytes()));

        assertEquals(2, rows.size());
        assertEquals("a", rows.get(0)[0]);
        assertEquals("2", rows.get(1)[1]);
    }

    @Test
    void s_shouldTrimString() {
        String[] row = {"  test  "};
        assertEquals("test", TsvParser.s(row, 0));
    }

    @Test
    void buildErrorTsv_shouldReturnBytes() {
        List<String[]> rows = List.<String[]>of(new String[]{"a", "b"});
        List<String> errors = List.of("err");

        byte[] result = TsvParser.buildErrorTsv(rows, errors);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

}
