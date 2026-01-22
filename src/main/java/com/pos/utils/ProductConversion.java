package com.pos.utils;

import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.ProductPojo;

public class ProductConversion {

    // For CREATE: No ID yet
    public static ProductPojo convertFormToPojo(ProductForm form) {
        ProductPojo p = new ProductPojo();
        p.setName(form.getName());
        p.setBarcode(form.getBarcode());
        p.setMrp(form.getMrp());
        // clientId will be set by the Flow layer
        // Explicitly check for empty or blank strings
        if (form.getImageUrl() == null || form.getImageUrl().isBlank()) {
            p.setImageUrl(null); // Force it to be null so the DB is happy
        } else {
            p.setImageUrl(form.getImageUrl());
        }

        return p;
    }

    // For DISPLAY: Pass ID explicitly as per your new Client pattern
    public static ProductData convertPojoToData(Integer id, ProductPojo p, String clientName) {
        ProductData d = new ProductData();
        d.setId(id);
        d.setName(p.getName());
        d.setBarcode(p.getBarcode());
        d.setMrp(p.getMrp());
        d.setClientName(clientName); // Resolved from Flow layer
        return d;
    }
}