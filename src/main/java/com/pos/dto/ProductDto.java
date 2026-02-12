package com.pos.dto;

import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.flow.ProductFlow;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.Product;
import com.pos.utils.ProductConversion;
import com.pos.utils.ProductTsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Component
public class ProductDto extends AbstractDto {

    @Autowired private ProductFlow productFlow;
    @Autowired private ProductApi productApi;

    public void add(ProductForm form) throws ApiException {
        normalize(form);
        validateForm(form);

        String clientName = normalize(form.getClientName());
        if (clientName == null) {
            throw new ApiException("clientName is required");
        }

        Product productPojo = ProductConversion.toPojo(form);
        productFlow.add(productPojo, clientName);
    }

    public void update(String barcode, ProductForm form) throws ApiException {
        normalize(form);
        validateForm(form);

        String clientName = normalize(form.getClientName());
        if (clientName == null) {
            throw new ApiException("clientName is required");
        }

        Product productPojo = ProductConversion.toPojo(form);
        productFlow.update(barcode, productPojo, clientName);
    }

    public void addBulk(Integer clientId, MultipartFile file) throws ApiException, IOException {
        if (clientId == null) {
            throw new ApiException("clientId is required for bulk upload");
        }

        ProductTsvParser.ProductTsvParseResult parsed = ProductTsvParser.parse(file);
        List<ProductForm> forms = parsed.forms();
        if (CollectionUtils.isEmpty(forms)) return;

        List<Product> products = new ArrayList<>(forms.size());
        for (ProductForm f : forms) {
            normalize(f);
            validateForm(f);
            products.add(ProductConversion.toPojo(f));
        }

        productFlow.addBulk(products, clientId);
    }

    public PaginatedResponse<ProductData> getProducts(String name, String barcode, String clientName, Integer pageNumber, Integer pageSize) throws ApiException {

        String normalizedName = normalize(name);
        String normalizedBarcode = normalize(barcode);
        String normalizedClientName = normalize(clientName);
//todo make custom class for this
        Map<String, Object> searchedProducts = productFlow.searchProducts(
                normalizedName, normalizedBarcode, normalizedClientName,
                pageNumber, pageSize
        );

        List<Product> products = (List<Product>) searchedProducts.get("products");
        Object totalObj = searchedProducts.get("total");
        long total = (totalObj instanceof Long) ? (Long) totalObj : ((Number) totalObj).longValue();

        Map<Integer, String> clientNameById = (Map<Integer, String>) searchedProducts.get("clientNameById");

        List<ProductData> dataList = new ArrayList<>();
        for (Product p : products) {
            String cName = clientNameById.getOrDefault(p.getClientId(), "Unknown Client");
            dataList.add(ProductConversion.toData(p, cName));
        }

        return PaginatedResponse.of(dataList, total, pageNumber);
    }
}
