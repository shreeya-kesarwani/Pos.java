package com.pos.dto;

import com.pos.flow.ProductFlow;
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
        f.setName(normalize(f.getName()));
        f.setBarcode(normalize(f.getBarcode()));
        f.setClientName(normalize(f.getClientName()));

        if (f.getMrp() != null && f.getMrp() <= 0) {
            throw new ApiException("MRP must be a positive number");
        }

        ProductPojo p = ProductConversion.convert(f);
        productFlow.add(p, f.getClientName());
    }

    // BYPASS FLOW: Call Service directly for update
    public void update(Integer id, ProductForm f) throws ApiException {
        f.setName(normalize(f.getName()));
        f.setBarcode(normalize(f.getBarcode()));
        ProductPojo p = ProductConversion.convert(f);
        productService.update(id, p);
    }

    public List<ProductData> getProducts(String name, String barcode, Integer clientId, String clientName, Integer page, Integer size) throws ApiException {
        List<ProductPojo> pojos;

        if (isSearch(name, barcode, clientId, clientName)) {
            // CALL FLOW: Search requires Client resolution
            pojos = productFlow.getAllFiltered(normalize(name), normalize(barcode), clientId, normalize(clientName));
        } else {
            // BYPASS FLOW: Pagination only needs Product table
            pojos = productService.getPaged(page, size);
        }

        return pojos.stream().map(this::toData).toList();
    }

    // BYPASS FLOW: Direct count
    public Long getCount() {
        return productService.getCount();
    }

    private ProductData toData(ProductPojo p) {
        ProductData d = ProductConversion.convert(p);
        // CALL FLOW: Conversion needs Client Name from ClientService
        d.setClientName(productFlow.getClientName(p.getClientId()));
        return d;
    }

    private boolean isSearch(String n, String b, Integer cId, String cName) {
        return (n != null && !n.isEmpty()) || (b != null && !b.isEmpty()) || cId != null || (cName != null && !cName.isEmpty());
    }
}