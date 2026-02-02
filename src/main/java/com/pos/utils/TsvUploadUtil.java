package com.pos.utils;

import com.pos.exception.ApiException;
import com.pos.exception.UploadValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TsvUploadUtil {

    private TsvUploadUtil() {}

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(String[] row, int lineNumber) throws ApiException;
    }

    /**
     * Reads TSV, validates header, maps rows, and throws UploadValidationException if any row has errors.
     */
    public static <T> List<T> parseOrThrow(
            List<String[]> rows,
            String errorFilePrefix,
            RowMapper<T> mapper
    ) throws ApiException {

        List<String> errors = new ArrayList<>();
        List<T> out = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] r = rows.get(i);
            String err = null;

            try {
                T obj = mapper.map(r, i + 1); // line number in file
                out.add(obj);
            } catch (ApiException ex) {
                err = "Line " + (i + 1) + ": " + ex.getMessage();
            } catch (Exception ex) {
                err = "Line " + (i + 1) + ": Invalid row";
            }

            errors.add(err);
        }

        boolean hasAnyError = errors.stream().anyMatch(Objects::nonNull);
        if (hasAnyError) {
            byte[] errorTsv = TsvParser.buildErrorTsv(rows, errors);
            String fname = errorFilePrefix + "_" +
                    LocalDateTime.now().toString().replace(":", "-") + ".tsv";

            throw new UploadValidationException(
                    "TSV has errors",
                    errorTsv,
                    fname,
                    "text/tab-separated-values"
            );
        }

        return out;
    }
}
