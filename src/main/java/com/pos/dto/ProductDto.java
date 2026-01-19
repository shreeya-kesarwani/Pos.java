package com.pos.dto;

import com.pos.flow.ProductFlow;
import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.ProductPojo;
import com.pos.service.ApiException;
import com.pos.utils.ProductConversion;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Component
@Validated
public class ProductDto extends AbstractDto {

    @Autowired private ProductFlow productFlow;
    @Autowired private ProductConversion productConversion;

    public void add(@Valid ProductForm f) throws ApiException {
        // 1. Normalization: Clean the strings
        f.setName(normalize(f.getName()));
        f.setBarcode(normalize(f.getBarcode()));
        f.setClientEmail(normalize(f.getClientEmail()));

        // 2. Validation: Ensure MRP is not negative
        if (f.getMrp() != null && f.getMrp() < 0) {
            throw new ApiException("MRP cannot be negative");
        }

        ProductPojo p = productConversion.convert(f);
        productFlow.add(p, f.getClientEmail());
    }

    public List<ProductData> getAll() throws ApiException {
        return productFlow.getAll().stream()
                .map(p -> productConversion.convert(p)) // Fixed type inference with lambda
                .toList();
    }

    public List<ProductData> getAllFiltered(String n, String b) throws ApiException {
        return productFlow.getAllFiltered(normalize(n), normalize(b)).stream()
                .map(p -> productConversion.convert(p))
                .toList();
    }

    public void update(Integer id, ProductForm f) throws ApiException {
        f.setName(normalize(f.getName()));
        f.setBarcode(normalize(f.getBarcode()));

        ProductPojo p = productConversion.convert(f);
        productFlow.update(id, p);
    }

    public void uploadTsv(List<ProductForm> forms) throws ApiException {
        for (ProductForm f : forms) {
            add(f); // This reuses your normalization and add logic
        }
    }
}