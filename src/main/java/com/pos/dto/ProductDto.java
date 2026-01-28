package com.pos.dto;

import com.pos.exception.ApiException;
import com.pos.flow.ProductFlow;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.Product;
import com.pos.utils.ProductConversion;
import com.pos.utils.TsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProductDto extends AbstractDto {

    @Autowired
    private ProductFlow productFlow;

    public void add(ProductForm form) throws ApiException {
        validateForm(form);
        normalize(form);

        Product productPojo = ProductConversion.convertFormToPojo(form);
        productFlow.add(productPojo, form.getClientName());
    }

    public void addBulkFromTsv(MultipartFile file) throws ApiException, IOException {
        List<ProductForm> forms = TsvParser.parseProductTsv(file.getInputStream());

        List<Product> products = new ArrayList<>();
        List<String> clientNames = new ArrayList<>();

        for (ProductForm form : forms) {
            validateForm(form);
            normalize(form);

            Product product = ProductConversion.convertFormToPojo(form);
            products.add(product);
            clientNames.add(form.getClientName());
        }

        productFlow.addBulk(products, clientNames);
    }

    public void update(String barcode, ProductForm form) throws ApiException {
        validateForm(form);
        normalize(form);

        Product product = ProductConversion.convertFormToPojo(form);
        productFlow.update(barcode, product, form.getClientName());
    }

    public PaginatedResponse<ProductData> getProducts(
            String name,
            String barcode,
            String clientName,
            Integer page,
            Integer size
    ) throws ApiException {

        int pageNumber = (page == null) ? 0 : page;
        int pageSize = (size == null) ? 10 : size;

        String normalizedName = normalize(name);
        String normalizedBarcode = normalize(barcode);
        String normalizedClientName = normalize(clientName);

        List<Product> pojos = productFlow.search(
                normalizedName, normalizedBarcode, normalizedClientName, pageNumber, pageSize
        );

        List<ProductData> dataList = pojos.stream()
                .map(pojo -> {
                    try {
                        String client = productFlow.getClientName(pojo.getClientId());
                        return ProductConversion.convertPojoToData(pojo.getId(), pojo, client);
                    } catch (Exception e) {
                        return ProductConversion.convertPojoToData(pojo.getId(), pojo, "Unknown Client");
                    }
                })
                .toList();

        Long totalCount = productFlow.getCount(
                normalizedName, normalizedBarcode, normalizedClientName
        );

        return PaginatedResponse.of(dataList, totalCount, pageNumber);
    }

    public Long getCount() {
        return productFlow.getCount(null, null, null);
    }
}
