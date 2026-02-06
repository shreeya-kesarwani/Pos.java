package com.pos.utils;

import com.pos.exception.ApiException;
import com.pos.exception.UploadValidationException;
import com.pos.model.form.InventoryForm;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class InventoryTsvParser {

    private InventoryTsvParser() {}

    public static InventoryTsvParseResult parse(MultipartFile file)
            throws ApiException, IOException {

        List<String[]> rows = TsvParser.read(file.getInputStream());
        TsvParser.validateHeader(rows.get(0), "barcode", "quantity");

        List<String> errors = new ArrayList<>();
        List<InventoryForm> forms = new ArrayList<>();
        List<String> barcodes = new ArrayList<>();

        Set<String> seenBarcodes = new HashSet<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] r = rows.get(i);
            String err = null;

            try {
                InventoryForm form = new InventoryForm();
                form.setBarcode(TsvParser.s(r, 0));

                String qStr = TsvParser.s(r, 1);
                if (qStr.isEmpty()) throw new ApiException("quantity is required");

                try {
                    form.setQuantity(Integer.parseInt(qStr));
                } catch (NumberFormatException e) {
                    throw new ApiException("Invalid quantity");
                }

                validateShape(form);
                normalizeShape(form);

                // ✅ Duplicate barcode check (after normalize so " abc " and "abc" match)
                String bc = form.getBarcode();
                if (!seenBarcodes.add(bc)) {
                    throw new ApiException("Duplicate barcode found in TSV: " + bc);
                }

                forms.add(form);
                barcodes.add(bc);

            } catch (ApiException ex) {
                err = ex.getMessage();
            } catch (Exception ex) {
                err = "Invalid row";
            }

            errors.add(err);
        }

        boolean hasError = errors.stream().anyMatch(Objects::nonNull);
        if (hasError) {
            byte[] errorTsv = TsvParser.buildErrorTsv(rows, errors);
            String fname = "inventory_upload_errors_" +
                    LocalDateTime.now().toString().replace(":", "-") + ".tsv";

            throw new UploadValidationException(
                    "TSV has errors",
                    errorTsv,
                    fname,
                    "text/tab-separated-values"
            );
        }

        return new InventoryTsvParseResult(forms, barcodes);
    }


    private static void validateShape(InventoryForm form) throws ApiException {
        if (form.getBarcode() == null || form.getBarcode().isBlank()) {
            throw new ApiException("barcode is required");
        }
        if (form.getQuantity() == null) {
            throw new ApiException("quantity is required");
        }
        if (form.getQuantity() < 0) {
            throw new ApiException("quantity cannot be negative");
        }
    }

    private static void normalizeShape(InventoryForm form) {
        form.setBarcode(form.getBarcode().trim());
    }

    public record InventoryTsvParseResult(
            List<InventoryForm> forms,
            List<String> barcodes
    ) {}
}
