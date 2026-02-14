package com.pos.utils;

import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.Product;

public class ProductConversion {

    private ProductConversion() {}

    public static Product toPojo(ProductForm form) {
        Product p = new Product();
        p.setName(form.getName());
        p.setBarcode(form.getBarcode());
        p.setMrp(form.getMrp());
        p.setClientId(form.getClientId());
        p.setImageUrl(normalizeBlankToNull(form.getImageUrl()));
        return p;
    }

    public static ProductData toData(Product p) {
        ProductData d = new ProductData();
        d.setId(p.getId());
        d.setName(p.getName());
        d.setBarcode(p.getBarcode());
        d.setMrp(p.getMrp());
        d.setImageUrl(p.getImageUrl());
        d.setClientId(p.getClientId());
        return d;
    }

    public static String normalizeBlankToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
