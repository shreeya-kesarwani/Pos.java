package com.pos.utils;

import com.pos.model.form.ProductForm;
import com.pos.pojo.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductUploadConversion {

    private ProductUploadConversion() {}

    public record BulkPayload(List<Product> products, List<String> clientNames) {}

    public static BulkPayload toBulkPayload(List<ProductForm> forms) {
        List<Product> products = new ArrayList<>(forms.size());
        List<String> clientNames = new ArrayList<>(forms.size());

        for (ProductForm f : forms) {
            clientNames.add(f.getClientName());
            products.add(ProductConversion.toPojo(f));
        }

        return new BulkPayload(products, clientNames);
    }
}
