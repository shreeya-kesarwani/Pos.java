package com.pos.dto;

import com.pos.exception.ApiException;
import com.pos.flow.ProductFlow;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.Product;
import com.pos.utils.ProductConversion;
import com.pos.utils.ProductTsvParser;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class ProductDto extends AbstractDto {

    @Autowired
    private ProductFlow productFlow;

    public void add(@Valid ProductForm form) throws ApiException {

        normalize(form);
        Product productPojo = ProductConversion.convertFormToPojo(form);
        productFlow.add(productPojo, form.getClientName());
    }

    public void update(String barcode, @Valid ProductForm form) throws ApiException {

        normalize(form);
        Product productPojo = ProductConversion.convertFormToPojo(form);
        productFlow.update(barcode, productPojo, form.getClientName());
    }

    public void addBulkFromTsv(MultipartFile file) throws ApiException, IOException {
        ProductTsvParser.ProductTsvParseResult parsed = ProductTsvParser.parse(file);
        productFlow.addBulkFromForms(parsed.forms());
    }

    public PaginatedResponse<ProductData> getProducts(
            String name,
            String barcode,
            String clientName,
            Integer pageNumber,
            Integer pageSize
    ) throws ApiException {

        String normalizedName = normalize(name);
        String normalizedBarcode = normalize(barcode);
        String normalizedClientName = normalize(clientName);

        return productFlow.searchWithClientNames(
                normalizedName,
                normalizedBarcode,
                normalizedClientName,
                pageNumber,
                pageSize
        );
    }

    public Long getCount() {
        return productFlow.getCount(null, null, null);
    }
}
