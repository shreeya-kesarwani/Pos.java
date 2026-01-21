package com.pos.utils;

import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.ProductPojo;

public class ProductConversion {

    // Method A: Pojo -> Data (Static)
    public static ProductData convert(ProductPojo productPojo) {
        if (productPojo == null) return null;
        ProductData data = new ProductData();
        data.setId(productPojo.getId());
        data.setBarcode(productPojo.getBarcode());
        data.setName(productPojo.getName());
        data.setMrp(productPojo.getMrp());
        data.setImageUrl(productPojo.getImageUrl());
        // Note: quantity and clientName need to be set in Flow/Dto layers
        return data;
    }

    // Method B: Form -> Pojo (Static)
    public static ProductPojo convert(ProductForm productForm) {
        if (productForm == null) return null;
        ProductPojo pojo = new ProductPojo();
        pojo.setBarcode(productForm.getBarcode());
        pojo.setName(productForm.getName());
        pojo.setMrp(productForm.getMrp());
        pojo.setImageUrl(productForm.getImageUrl());
        return pojo;
    }
}