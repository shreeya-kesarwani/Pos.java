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
        TsvParser.validateHeader(rows.get(0), "barcode", "clientname", "name", "mrp", "imageurl");

        List<String> errors = new ArrayList<>();
        List<ProductForm> validForms = new ArrayList<>();
        Set<String> uniqueClientNames = new HashSet<>();
        Set<String> seenBarcodes = new HashSet<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] r = rows.get(i);
            String err = null;

            try {
                ProductForm form = new ProductForm();
                form.setBarcode(TsvParser.s(r, 0));
                form.setClientName(TsvParser.s(r, 1));
                form.setName(TsvParser.s(r, 2));

                String mrpStr = TsvParser.s(r, 3);
                if (mrpStr.isEmpty()) throw new ApiException("mrp is required");
                try {
                    form.setMrp(Double.parseDouble(mrpStr));
                } catch (NumberFormatException e) {
                    throw new ApiException("Invalid mrp");
                }

                String img = TsvParser.s(r, 4);
                form.setImageUrl(img.isEmpty() ? null : img);

                // structural/shape validation only (NOT DB checks)
                validateShape(form);

                // normalize (trim/lowercase etc) - still not business validation
                normalizeShape(form);

                // duplicates within file (still not business validation)
                String bc = form.getBarcode();
                if (!seenBarcodes.add(bc)) {
                    throw new ApiException("Duplicate barcode in file: " + bc);
                }

                uniqueClientNames.add(form.getClientName());
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

        return new ProductTsvParseResult(validForms, uniqueClientNames);
    }

    private static void validateShape(ProductForm form) throws ApiException {
        if (form.getBarcode() == null || form.getBarcode().isBlank()) throw new ApiException("barcode is required");
        if (form.getClientName() == null || form.getClientName().isBlank()) throw new ApiException("clientname is required");
        if (form.getName() == null || form.getName().isBlank()) throw new ApiException("name is required");
        if (form.getMrp() == null) throw new ApiException("mrp is required");
        if (form.getMrp() < 0) throw new ApiException("mrp cannot be negative");
    }

    private static void normalizeShape(ProductForm form) {
        form.setBarcode(form.getBarcode().trim());
        form.setClientName(form.getClientName().trim());
        form.setName(form.getName().trim());
        // imageUrl already trimmed by TsvParser.s
        if (form.getImageUrl() != null && form.getImageUrl().isBlank()) form.setImageUrl(null);
    }

    public record ProductTsvParseResult(List<ProductForm> forms, Set<String> clientNames) {}
}
