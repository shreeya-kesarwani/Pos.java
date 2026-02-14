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
        Product productPojo = ProductConversion.toPojo(form);
        productFlow.add(productPojo);
    }

    public void update(Integer productId, ProductForm form) throws ApiException {
        normalize(form);
        validateForm(form);
        Product product = ProductConversion.toPojo(form);
        productApi.update(productId, product);
    }

    public void addBulk(Integer clientId, MultipartFile file) throws ApiException, IOException {

        ProductTsvParser.ProductTsvParseResult parsed = ProductTsvParser.parse(file, clientId);
        List<ProductForm> forms = parsed.forms();
        if (CollectionUtils.isEmpty(forms)) return;

        List<Product> products = new ArrayList<>(forms.size());
        for (ProductForm f : forms) {
            normalize(f);
            validateForm(f);
            Product product = ProductConversion.toPojo(f);
            products.add(product);
        }
        productFlow.addBulk(products, clientId);
    }

    public PaginatedResponse<ProductData> getProducts(String name, String barcode, Integer clientId, Integer pageNumber, Integer pageSize) throws ApiException {

        name = normalize(name);
        barcode = normalize(barcode);

        List<Product> products = productApi.search(name, barcode, clientId, pageNumber, pageSize);
        long total = productApi.getCount(name, barcode, clientId);

        List<ProductData> dataList = new ArrayList<>(products.size());
        for (Product p : products) {
            dataList.add(ProductConversion.toData(p));
        }

        return PaginatedResponse.of(dataList, total, pageNumber);
    }
}
