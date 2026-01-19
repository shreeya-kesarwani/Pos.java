package com.pos.utils;

import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.ProductPojo;

public class ProductConversion {

    // Method A: Pojo -> Data
    public static ProductData convert(ProductPojo productPojo) {
        if (productPojo == null) return null;
        ProductData data = new ProductData();
        return data;
    }

    // Method B: Form -> Pojo
    public static ProductPojo convert(ProductForm productForm) {
        if (productForm == null) return null;
        ProductPojo pojo = new ProductPojo();
        return pojo;
    }
}