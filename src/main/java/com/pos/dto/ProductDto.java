package com.pos.dto;

import com.pos.flow.ProductFlow;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.ProductPojo;
import com.pos.service.ApiException;
import com.pos.service.ProductService;
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
    @Autowired private ProductService productService;

    public void add(@Valid ProductForm f) throws ApiException {
        validateForm(f);
        normalize(f);

        validatePositive(f.getMrp(), "MRP");

        ProductPojo p = ProductConversion.convertFormToPojo(f);
        productFlow.add(p, f.getClientName());
    }

    public void update(String barcode, @Valid ProductForm f) throws ApiException {
        validateForm(f);
        normalize(f);
        validatePositive(f.getMrp(), "MRP");

        ProductPojo p = ProductConversion.convertFormToPojo(f);
        productFlow.update(barcode, p, f.getClientName());
    }

    public PaginatedResponse<ProductData> getProducts(String name, String barcode, String clientName, Integer page, Integer size) throws ApiException {
        int p = (page == null) ? 0 : page;
        int s = (size == null) ? 10 : size;

        String nName = normalize(name);
        String nBarcode = normalize(barcode);
        String nClientName = normalize(clientName);

        List<ProductPojo> pojos = productFlow.search(nName, nBarcode, nClientName, p, s);
        List<ProductData> dataList = pojos.stream()
                .map(pojo -> {
                    try {
                        String cName = productFlow.getClientName(pojo.getClientId());
                        return ProductConversion.convertPojoToData(pojo.getId(), pojo, cName);
                    } catch (ApiException e) {
                        // Re-throw as a RuntimeException so the stream can handle it
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        return PaginatedResponse.of(dataList, productFlow.getCount(nName, nBarcode, nClientName), p);
    }

    public Long getCount() {
        return productService.getCount(null, null, null);
    }

    public void addBulk(List<ProductForm> forms) throws ApiException {
        for (ProductForm f : forms) {
            // Reuse your existing logic for validation and normalization
            validateForm(f);
            normalize(f);
            validatePositive(f.getMrp(), "MRP");

            ProductPojo p = ProductConversion.convertFormToPojo(f);
            productFlow.add(p, f.getClientName());
        }
    }
}