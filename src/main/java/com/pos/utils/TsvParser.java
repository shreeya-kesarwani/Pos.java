package com.pos.utils;

import com.pos.model.form.InventoryForm;
import com.pos.model.form.ProductForm;
import com.pos.exception.ApiException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TsvParser {
    //TODO - see if somethings can be made as private method, refactor it, make it cleaner
    public static List<ProductForm> parseProductTsv(InputStream is) throws ApiException {
        List<ProductForm> forms = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                // FIX: Skip the header row
                if (lineNumber == 1 && line.toLowerCase().contains("barcode")) {
                    continue;
                }

                String[] columns = line.split("\t");

                // Check for minimum required columns (Barcode, ClientName, Name, MRP)
                //TODO instead of hardcoding the number, params me class jana chahiye(productForm.class), uske form se length lelo, using java reflections
                //TODO - check the header names, if not matching, return error tsv and reject it
                if (columns.length < 4) {
                    throw new ApiException("Error at line " + lineNumber + ": Missing columns. Expected barcode, clientName, name, mrp.");
                }

                ProductForm form = new ProductForm();
                form.setBarcode(columns[0].trim());
                form.setClientName(columns[1].trim());
                form.setName(columns[2].trim());

                try {
                    form.setMrp(Double.parseDouble(columns[3].trim()));
                } catch (NumberFormatException e) {
                    throw new ApiException("Error at line " + lineNumber + ": Invalid MRP value [" + columns[3] + "].");
                }

                // Image URL is optional (5th column)
                if (columns.length >= 5) {
                    String url = columns[4].trim();
                    form.setImageUrl(url.isEmpty() ? null : url);
                }

                forms.add(form);
            }
        } catch (ApiException e) {
            throw e; // Re-throw our custom error
        } catch (Exception e) {
            throw new ApiException("Failed to parse Product TSV file: " + e.getMessage());
        }
        return forms;
    }

    public static List<InventoryForm> parseInventoryTsv(InputStream is) throws ApiException {
        List<InventoryForm> forms = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                // SKIP HEADER: If line 1 contains "barcode", skip it
                if(lineNumber == 1 && line.toLowerCase().contains("barcode")) continue;

                String[] columns = line.split("\t");
                if (columns.length < 2) throw new ApiException("Invalid columns at line " + lineNumber);

                InventoryForm form = new InventoryForm();
                form.setBarcode(columns[0].trim());
                form.setQuantity(Integer.parseInt(columns[1].trim()));
                forms.add(form);
            }
        } catch (Exception e) {
            throw new ApiException("Error parsing Inventory TSV: " + e.getMessage());
        }
        return forms;
    }

}