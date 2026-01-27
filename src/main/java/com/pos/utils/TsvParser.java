package com.pos.utils;

import com.pos.exception.ApiException;
import com.pos.model.form.InventoryForm;
import com.pos.model.form.ProductForm;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TsvParser {

    public static List<ProductForm> parseProductTsv(InputStream is) throws ApiException {
        List<String[]> rows = readTsv(is);

        validateHeader(
                rows.get(0),
                new String[]{"barcode", "clientname", "name", "mrp", "imageurl"}
        );

        int expectedColumns = getFieldCount(ProductForm.class);

        List<ProductForm> forms = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            String[] cols = rows.get(i);

            if (cols.length < expectedColumns - 1) {
                throw new ApiException("Invalid column count at line " + (i + 1));
            }

            ProductForm form = new ProductForm();
            form.setBarcode(cols[0].trim());
            form.setClientName(cols[1].trim());
            form.setName(cols[2].trim());

            try {
                form.setMrp(Double.parseDouble(cols[3].trim()));
            } catch (NumberFormatException e) {
                throw new ApiException("Invalid MRP at line " + (i + 1));
            }

            if (cols.length >= 5 && !cols[4].trim().isEmpty()) {
                form.setImageUrl(cols[4].trim());
            }

            forms.add(form);
        }

        return forms;
    }

    public static List<InventoryForm> parseInventoryTsv(InputStream is) throws ApiException {
        List<String[]> rows = readTsv(is);

        validateHeader(
                rows.get(0),
                new String[]{"barcode", "quantity"}
        );

        int expectedColumns = getFieldCount(InventoryForm.class);

        List<InventoryForm> forms = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            String[] cols = rows.get(i);

            if (cols.length < expectedColumns) {
                throw new ApiException("Invalid column count at line " + (i + 1));
            }

            InventoryForm form = new InventoryForm();
            form.setBarcode(cols[0].trim());
            form.setQuantity(Integer.parseInt(cols[1].trim()));

            forms.add(form);
        }

        return forms;
    }

    private static List<String[]> readTsv(InputStream is) throws ApiException {
        List<String[]> rows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    rows.add(line.split("\t"));
                }
            }
        } catch (Exception e) {
            throw new ApiException("Failed to read TSV file");
        }

        if (rows.isEmpty()) {
            throw new ApiException("Empty TSV file");
        }

        return rows;
    }

    private static void validateHeader(String[] actual, String[] expected) throws ApiException {
        if (actual.length < expected.length) {
            throw new ApiException("Invalid header length");
        }

        for (int i = 0; i < expected.length; i++) {
            if (!actual[i].trim().equalsIgnoreCase(expected[i])) {
                throw new ApiException(
                        "Invalid header. Expected '" + expected[i] + "' but found '" + actual[i] + "'"
                );
            }
        }
    }

    private static int getFieldCount(Class<?> clazz) {
        int count = 0;
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isSynthetic()) {
                count++;
            }
        }
        return count;
    }
}
