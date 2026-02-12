package com.pos.utils;

import com.pos.exception.ApiException;
import com.pos.exception.UploadValidationException;
import com.pos.model.form.ProductForm;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class ProductTsvParser {

    private ProductTsvParser() {}

    public static ProductTsvParseResult parse(MultipartFile file) throws ApiException, IOException {
        List<String[]> rows = TsvParser.read(file.getInputStream());

        if (rows == null || rows.isEmpty()) {
            throw new ApiException("Empty TSV file");
        }

        // Header: allow 3 cols (barcode,name,mrp) OR 4 cols (barcode,name,mrp,imageurl)
        // clientname removed completely
        try {
            validateFlexibleHeader(rows.get(0));
        } catch (ApiException headerEx) {
            List<String> errors = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                errors.add("Invalid header: " + headerEx.getMessage());
            }

            byte[] errorTsv = TsvParser.buildErrorTsv(rows, errors);
            String fname = "product_upload_errors_" + LocalDateTime.now().toString().replace(":", "-") + ".tsv";

            throw new UploadValidationException(
                    "TSV has errors",
                    errorTsv,
                    fname,
                    "text/tab-separated-values"
            );
        }

        List<String> errors = new ArrayList<>();
        List<ProductForm> validForms = new ArrayList<>();
        Set<String> seenBarcodes = new HashSet<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] r = rows.get(i);
            String err = null;

            try {
                ProductForm form = new ProductForm();

                form.setBarcode(TsvParser.s(r, 0));
                form.setName(TsvParser.s(r, 1));

                String mrpStr = TsvParser.s(r, 2);
                if (mrpStr.isEmpty()) throw new ApiException("mrp is required");
                try {
                    form.setMrp(Double.parseDouble(mrpStr));
                } catch (NumberFormatException e) {
                    throw new ApiException("Invalid mrp");
                }

                // imageurl OPTIONAL:
                // - header may have 3 or 4 cols
                // - row may have missing trailing column
                String img = (r != null && r.length > 3) ? TsvParser.s(r, 3) : "";
                form.setImageUrl(img.isEmpty() ? null : img);

                validateShape(form);
                normalizeShape(form);

                String bc = form.getBarcode();
                if (!seenBarcodes.add(bc)) {
                    throw new ApiException("Duplicate barcode in file: " + bc);
                }

                validForms.add(form);

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
            String fname = "product_upload_errors_" + LocalDateTime.now().toString().replace(":", "-") + ".tsv";

            throw new UploadValidationException(
                    "TSV has errors",
                    errorTsv,
                    fname,
                    "text/tab-separated-values"
            );
        }

        return new ProductTsvParseResult(validForms);
    }

    private static void validateFlexibleHeader(String[] headerRow) throws ApiException {
        if (headerRow == null || headerRow.length == 0) {
            throw new ApiException("Missing header");
        }

        // normalize header cells (trim + lowercase)
        List<String> h = new ArrayList<>();
        for (String cell : headerRow) {
            String v = (cell == null) ? "" : cell.trim().toLowerCase();
            if (!v.isEmpty()) h.add(v);
        }

        // IMPORTANT:
        // - If your TsvParser.read keeps empty trailing columns, this still works.
        // - If it does not, we also accept 3-col header.
        if (h.size() == 3) {
            if (!h.get(0).equals("barcode") || !h.get(1).equals("name") || !h.get(2).equals("mrp")) {
                throw new ApiException("Expected header: barcode, name, mrp (optional imageurl)");
            }
            return;
        }

        if (h.size() == 4) {
            if (!h.get(0).equals("barcode") || !h.get(1).equals("name") || !h.get(2).equals("mrp") || !h.get(3).equals("imageurl")) {
                throw new ApiException("Expected header: barcode, name, mrp, imageurl");
            }
            return;
        }

        throw new ApiException("Invalid header length (expected 3 or 4 columns)");
    }

    private static void validateShape(ProductForm form) throws ApiException {
        if (form.getBarcode() == null || form.getBarcode().isBlank()) throw new ApiException("barcode is required");
        if (form.getName() == null || form.getName().isBlank()) throw new ApiException("name is required");
        if (form.getMrp() == null) throw new ApiException("mrp is required");
        if (form.getMrp() < 0) throw new ApiException("mrp cannot be negative");
        // imageUrl optional
    }

    private static void normalizeShape(ProductForm form) {
        form.setBarcode(form.getBarcode().trim());
        form.setName(form.getName().trim());
        if (form.getImageUrl() != null && form.getImageUrl().isBlank()) {
            form.setImageUrl(null);
        }
    }

    public record ProductTsvParseResult(List<ProductForm> forms) {}
}
