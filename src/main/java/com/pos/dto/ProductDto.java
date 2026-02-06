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

        List<Product> products = productApi.search(
                normalizedName, normalizedBarcode, normalizedClientName,
                pageNumber, pageSize
        );

        Set<Integer> clientIds = products.stream()
                .map(Product::getClientId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, String> clientNameById;
        if (clientIds.isEmpty()) {
            clientNameById = Map.of();
        } else {
            List<Client> clients = clientApi.getByIds(new ArrayList<>(clientIds));
            clientNameById = clients.stream()
                    .collect(Collectors.toMap(Client::getId, Client::getName, (a, b) -> a));
        }

        List<ProductData> dataList = products.stream()
                .map(p -> ProductConversion.toData(
                        p,
                        clientNameById.getOrDefault(p.getClientId(), "Unknown Client")
                ))
                .toList();

        Long total = productApi.getCount(normalizedName, normalizedBarcode, normalizedClientName);
        return PaginatedResponse.of(dataList, total, pageNumber);
    }

    public Long getCount() {
        return productApi.getCount(null, null, null);
    }
}
