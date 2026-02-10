package com.pos.dto;

import com.pos.api.ClientApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.flow.ProductFlow;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.Client;
import com.pos.pojo.Product;
import com.pos.utils.ProductConversion;
import com.pos.utils.ProductTsvParser;
import com.pos.utils.ProductUploadConversion;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProductDto extends AbstractDto {

    @Autowired private ProductFlow productFlow;
    @Autowired private ProductApi productApi;
    @Autowired private ClientApi clientApi;

    public void add(@Valid ProductForm form) throws ApiException {
        normalize(form);
        Product productPojo = ProductConversion.toPojo(form);
        productFlow.add(productPojo, form.getClientName());
    }

    public void update(String barcode, @Valid ProductForm form) throws ApiException {
        normalize(form);
        Product productPojo = ProductConversion.toPojo(form);
        productFlow.update(barcode, productPojo, form.getClientName());
    }

    public void addBulk(MultipartFile file) throws ApiException, IOException {
        ProductTsvParser.ProductTsvParseResult parsed = ProductTsvParser.parse(file);

        List<ProductForm> forms = parsed.forms();
        if (CollectionUtils.isEmpty(forms)) return;

        forms.forEach(this::normalize);

        ProductUploadConversion.BulkPayload payload =
                ProductUploadConversion.toBulkPayload(forms);

        productFlow.addBulk(payload.products(), payload.clientNames());
    }

    public PaginatedResponse<ProductData> getProducts(String name, String barcode, String clientName, Integer pageNumber, Integer pageSize) throws ApiException {

        String normalizedName = normalize(name);
        String normalizedBarcode = normalize(barcode);
        String normalizedClientName = normalize(clientName);

        Map<String, Object> result = productFlow.searchProducts(
                normalizedName, normalizedBarcode, normalizedClientName,
                pageNumber, pageSize
        );

        List<Product> products = (List<Product>) result.get("products");

        Object totalObj = result.get("total");
        long total = (totalObj instanceof Long) ? (Long) totalObj : ((Number) totalObj).longValue();

        Map<Integer, String> clientNameById = (Map<Integer, String>) result.get("clientNameById");

        List<ProductData> dataList = new ArrayList<>();
        for (Product p : products) {
            String cName = clientNameById.getOrDefault(p.getClientId(), "Unknown Client");
            dataList.add(ProductConversion.toData(p, cName));
        }
        return PaginatedResponse.of(dataList, total, pageNumber);
    }


    public Long getCount() {
        return productApi.getCount(null, null, null);
    }
}
