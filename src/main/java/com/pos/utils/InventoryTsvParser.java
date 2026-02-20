package com.pos.utils;

import com.pos.exception.ApiException;
import com.pos.exception.UploadValidationException;
import com.pos.model.form.InventoryForm;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static com.pos.model.constants.ErrorMessages.*;

public class InventoryTsvParser {

    private InventoryTsvParser() {}

    public static List<InventoryForm> parse(MultipartFile file) throws ApiException, IOException {

        List<String[]> rows = TsvParser.read(file.getInputStream());
        TsvParser.validateHeader(rows.get(0), "barcode", "quantity");

        List<String> errors = new ArrayList<>();
        List<InventoryForm> forms = new ArrayList<>();
        Set<String> seenBarcodes = new HashSet<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] r = rows.get(i);
            String err = null;

            try {
                InventoryForm form = new InventoryForm();
                form.setBarcode(TsvParser.s(r, 0));

                String qStr = TsvParser.s(r, 1);
                if (qStr.isEmpty()) throw new ApiException(QUANTITY_REQUIRED.value());

                try {
                    form.setQuantity(Integer.parseInt(qStr));
                } catch (NumberFormatException e) {
                    throw new ApiException(INVALID_QUANTITY.value());
                }

                validateShape(form);
                normalizeShape(form);

                String bc = form.getBarcode();
                if (!seenBarcodes.add(bc)) {
                    throw new ApiException(INVALID_ROW.value());
                }

                forms.add(form);

            } catch (ApiException ex) {
                err = ex.getMessage();
            } catch (Exception ex) {
                err = INVALID_ROW.value();
            }

            errors.add(err);
        }

        boolean hasError = errors.stream().anyMatch(Objects::nonNull);
        if (hasError) {
            byte[] errorTsv = TsvParser.buildErrorTsv(rows, errors);
            String fname = "inventory_upload_errors_" +
                    LocalDateTime.now().toString().replace(":", "-") + ".tsv";

            throw new UploadValidationException(
                    TSV_HAS_ERRORS.value(),
                    errorTsv,
                    fname,
                    "text/tab-separated-values"
            );
        }

        return forms;
    }

    private static void validateShape(InventoryForm form) throws ApiException {
        if (form.getBarcode() == null || form.getBarcode().isBlank()) {
            throw new ApiException(BARCODE_REQUIRED.value());
        }
        if (form.getQuantity() == null) {
            throw new ApiException(QUANTITY_REQUIRED.value());
        }
        if (form.getQuantity() < 0) {
            throw new ApiException(QUANTITY_CANNOT_BE_NEGATIVE.value());
        }
    }

    private static void normalizeShape(InventoryForm form) {
        form.setBarcode(form.getBarcode().trim());
    }
}