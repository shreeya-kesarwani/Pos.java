package com.pos.utils;

import com.pos.exception.ApiException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.pos.model.constants.ErrorMessages.*;

public class TsvParser {

    private TsvParser() {}

    public static List<String[]> read(InputStream is) throws ApiException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            List<String[]> rows = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) rows.add(line.split("\t", -1));
            }
            if (rows.isEmpty()) throw new ApiException(EMPTY_TSV_FILE.value());
            return rows;
        } catch (IOException e) {
            throw new ApiException(FAILED_TO_READ_TSV_FILE.value());
        }
    }

    public static void validateHeader(String[] actual, String... expected) throws ApiException {
        if (actual == null || expected == null) throw new ApiException(INVALID_TSV_HEADER.value());
        if (actual.length < expected.length) throw new ApiException(INVALID_TSV_HEADER_LENGTH.value());

        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equalsIgnoreCase(s(actual, i))) {
                throw new ApiException(INVALID_TSV_HEADER.value());
            }
        }
    }

    public static String s(String[] row, int idx) {
        if (row == null || idx < 0 || idx >= row.length) return "";
        return row[idx] == null ? "" : row[idx].trim();
    }

    public static byte[] buildErrorTsv(List<String[]> originalRows, List<String> rowErrors) {
        StringBuilder sb = new StringBuilder();

        String[] header = originalRows.get(0);
        sb.append(join(header)).append("\t").append("error").append("\n");

        for (int i = 1; i < originalRows.size(); i++) {
            String[] row = originalRows.get(i);
            String err = rowErrors.get(i - 1);
            sb.append(join(row)).append("\t").append(err == null ? "" : err).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String join(String[] cols) {
        if (cols == null || cols.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) sb.append("\t");
            sb.append(cols[i] == null ? "" : cols[i]);
        }
        return sb.toString();
    }
}