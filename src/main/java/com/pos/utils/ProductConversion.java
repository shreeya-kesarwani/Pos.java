package com.pos.utils;

import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.Product;

public class ProductConversion {

    public static Product convertFormToPojo(ProductForm form) {
        Product p = new Product();
        p.setName(form.getName());
        p.setBarcode(form.getBarcode());
        p.setMrp(form.getMrp());
        if (form.getImageUrl() == null || form.getImageUrl().isBlank()) {
            p.setImageUrl(null);
        } else {
            p.setImageUrl(form.getImageUrl());
        }

        return p;
    }

    public static ProductData convertPojoToData(Integer id, Product p, String clientName) {
        ProductData d = new ProductData();
        d.setId(id);
        d.setName(p.getName());
        d.setBarcode(p.getBarcode());
        d.setMrp(p.getMrp());
        d.setClientName(clientName);
        return d;
    }
}