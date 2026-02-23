package com.pos.utils;

import com.pos.exception.ApiException;
import com.pos.exception.UploadValidationException;
import com.pos.model.form.ProductForm;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static com.pos.model.constants.ErrorMessages.*;

public class ProductTsvParser {

    private ProductTsvParser() {}

    public static ProductTsvParseResult parse(MultipartFile file, Integer clientId) throws ApiException, IOException {
        if (clientId == null) {
            throw new ApiException(CLIENT_ID_REQUIRED.value());
        }

        List<String[]> rows = TsvParser.read(file.getInputStream());
        if (rows == null || rows.isEmpty()) {
            throw new ApiException("Empty TSV file");
        }

        try {
            validateFlexibleHeader(rows.get(0));
        } catch (ApiException headerEx) {
            List<String> errors = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                errors.add(headerEx.getMessage());
            }

            byte[] errorTsv = TsvParser.buildErrorTsv(rows, errors);
            String fname = "product_upload_errors_" + LocalDateTime.now().toString().replace(":", "-") + ".tsv";

            throw new UploadValidationException(
                    TSV_HAS_ERRORS.value(),
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
                if (mrpStr.isEmpty()) throw new ApiException(MRP_REQUIRED.value());
                try {
                    form.setMrp(Double.parseDouble(mrpStr));
                } catch (NumberFormatException e) {
                    throw new ApiException(INVALID_MRP.value());
                }

                String img = (r != null && r.length > 3) ? TsvParser.s(r, 3) : "";
                form.setImageUrl(img.isEmpty() ? null : img);

                form.setClientId(clientId);

                validateShape(form);
                normalizeShape(form);

                String bc = form.getBarcode();
                if (!seenBarcodes.add(bc)) {
                    throw new ApiException(INVALID_ROW.value());
                }

                validForms.add(form);

            } catch (ApiException ex) {
                err = "Line " + (i + 1) + ": " + ex.getMessage();
            } catch (Exception ex) {
                err = "Line " + (i + 1) + ": " + INVALID_ROW.value();
            }

            errors.add(err);
        }

        boolean hasAnyError = errors.stream().anyMatch(Objects::nonNull);
        if (hasAnyError) {
            byte[] errorTsv = TsvParser.buildErrorTsv(rows, errors);
            String fname = "product_upload_errors_" + LocalDateTime.now().toString().replace(":", "-") + ".tsv";

            throw new UploadValidationException(
                    TSV_HAS_ERRORS.value(),
                    errorTsv,
                    fname,
                    "text/tab-separated-values"
            );
        }

        return new ProductTsvParseResult(validForms);
    }

    private static void validateFlexibleHeader(String[] headerRow) throws ApiException {
        if (headerRow == null || headerRow.length == 0) {
            throw new ApiException(MISSING_TSV_HEADER.value());
        }

        List<String> h = new ArrayList<>();
        for (String cell : headerRow) {
            String v = (cell == null) ? "" : cell.trim().toLowerCase();
            if (!v.isEmpty()) h.add(v);
        }

        if (h.size() == 3) {
            if (!h.get(0).equals("barcode") || !h.get(1).equals("name") || !h.get(2).equals("mrp")) {
                throw new ApiException(INVALID_TSV_HEADER.value());
            }
            return;
        }

        if (h.size() == 4) {
            if (!h.get(0).equals("barcode") || !h.get(1).equals("name") || !h.get(2).equals("mrp") || !h.get(3).equals("imageurl")) {
                throw new ApiException(INVALID_TSV_HEADER.value());
            }
            return;
        }

        throw new ApiException(INVALID_TSV_HEADER_LENGTH.value());
    }

    private static void validateShape(ProductForm form) throws ApiException {
        if (form.getBarcode() == null || form.getBarcode().isBlank()) throw new ApiException(BARCODE_REQUIRED.value());
        if (form.getName() == null || form.getName().isBlank()) throw new ApiException(NAME_REQUIRED.value());
        if (form.getMrp() == null) throw new ApiException(MRP_REQUIRED.value());
        if (form.getMrp() < 0) throw new ApiException(MRP_CANNOT_BE_NEGATIVE.value());
        if (form.getClientId() == null) throw new ApiException(CLIENT_ID_REQUIRED.value());
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