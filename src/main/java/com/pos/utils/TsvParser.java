package com.pos.utils;

import com.pos.model.form.InventoryForm;
import com.pos.model.form.ProductForm;
import com.pos.service.ApiException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TsvParser {

    // Parses the TSV file into a list of ProductForms
    public static List<ProductForm> parseProductTsv(InputStream is) throws ApiException {
        List<ProductForm> forms = new ArrayList<>();
        // Using try-with-resources to ensure the stream closes
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue; // Skip empty lines

                // Split line by tab character
                String[] columns = line.split("\t");

                // Expecting 5 columns: barcode, email, name, mrp, imageUrl
                if (columns.length < 4) {
                    throw new ApiException("Error at line " + lineNumber + ": Missing columns. Expected barcode, email, name, mrp.");
                }

                ProductForm form = new ProductForm();
                form.setBarcode(columns[0].trim());
                form.setClientEmail(columns[1].trim());
                form.setName(columns[2].trim());

                try {
                    form.setMrp(Double.parseDouble(columns[3].trim()));
                } catch (NumberFormatException e) {
                    throw new ApiException("Error at line " + lineNumber + ": Invalid MRP value.");
                }

                // Image URL is optional in the 5th column
                if (columns.length >= 5) {
                    form.setImageUrl(columns[4].trim());
                }

                forms.add(form);
            }
        } catch (Exception e) {
            throw new ApiException("Failed to parse TSV file: " + e.getMessage());
        }
        return forms;
    }

    public static List<InventoryForm> parseInventoryTsv(InputStream is) throws ApiException {
        List<InventoryForm> forms = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] columns = line.split("\t");
                if (columns.length < 2) throw new ApiException("Invalid TSV format for Inventory");

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