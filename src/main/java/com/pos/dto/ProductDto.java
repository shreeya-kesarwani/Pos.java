package com.pos.dto;

import com.pos.flow.ProductFlow;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.Product;
import com.pos.exception.ApiException;
import com.pos.service.ProductService;
import com.pos.utils.ProductConversion;
import com.pos.utils.TsvParser;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Validated
public class ProductDto extends AbstractDto {

    @Autowired private ProductFlow productFlow;
    @Autowired private ProductService productService;

    public void add(@Valid ProductForm form) throws ApiException {
//        validateForm(form);
        normalize(form);
        validatePositive(form.getMrp(), "MRP");

        Product productPojo = ProductConversion.convertFormToPojo(form);
        productFlow.add(productPojo, form.getClientName());
    }

    private void validateAndNormalize(ProductForm f) throws ApiException {
        validateForm(f);
        normalize(f);
        validatePositive(f.getMrp(), "MRP");
    }

    public void addBulkFromTsv(MultipartFile file) throws ApiException, IOException {
        // 1. Parsing happens here in DTO now
        List<ProductForm> forms = TsvParser.parseProductTsv(file.getInputStream());

        List<Product> pojos = new ArrayList<>();
        List<String> clientNames = new ArrayList<>();

        // 2. Collect everything into lists
        for (ProductForm f : forms) {
            validateAndNormalize(f);
            Product p = ProductConversion.convertFormToPojo(f);
            pojos.add(p);
            clientNames.add(f.getClientName());
        }

        // 3. Hand off the lists to Flow for a single Transaction
        productFlow.addBulk(pojos, clientNames);
    }

    public void update(String barcode, @Valid ProductForm f) throws ApiException {
        validateForm(f);
        normalize(f);
        validatePositive(f.getMrp(), "MRP");

        Product p = ProductConversion.convertFormToPojo(f);
        productFlow.update(barcode, p, f.getClientName());
    }

    public PaginatedResponse<ProductData> getProducts(String name, String barcode, String clientName, Integer page, Integer size) throws ApiException {
//        can be handled by controller
        int p = (page == null) ? 0 : page;
        int s = (size == null) ? 10 : size;

        String nName = normalize(name);
        String nBarcode = normalize(barcode);
        String nClientName = normalize(clientName);

        List<Product> pojos = productFlow.search(nName, nBarcode, nClientName, p, s);
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
        //loop for storing in list, list will go to flow, flow will add the list (as it is transactional)
        for (ProductForm f : forms) {
            //one private method for validation+normalisation combined
            validateForm(f);
            normalize(f);
            validatePositive(f.getMrp(), "MRP");
            Product p = ProductConversion.convertFormToPojo(f);
            productFlow.add(p, f.getClientName());
        }
    }
}