package com.pos.flow;

import com.pos.api.ClientApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.Client;
import com.pos.pojo.Product;
import com.pos.utils.ProductConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional(rollbackFor = ApiException.class)
public class ProductFlow {

    @Autowired
    ProductApi productApi;
    @Autowired
    ClientApi clientApi;

    public void add(Product p, String clientName) throws ApiException {
        Integer clientId = getClientIdByName(clientName);
        p.setClientId(clientId);
        productApi.add(p);
    }

    public void update(String barcode, Product p, String clientName) throws ApiException {
        Integer clientId = getClientIdByName(clientName);
        p.setClientId(clientId);
        productApi.update(barcode, p);
    }

    public void addBulkFromForms(List<ProductForm> forms) throws ApiException {

        Set<String> clientNames = forms.stream()
                .map(ProductForm::getClientName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        if (clientNames.isEmpty()) {
            throw new ApiException("Client name is required");
        }

        List<Client> clients = clientApi.getByNames(new ArrayList<>(clientNames));
        Map<String, Integer> clientIdByName = clients.stream()
                .collect(Collectors.toMap(
                        Client::getName,
                        Client::getId,
                        (a, b) -> a
                ));

        for (String cn : clientNames) {
            if (!clientIdByName.containsKey(cn)) {
                throw new ApiException("Client not found: " + cn);
            }
        }

        List<Product> products = new ArrayList<>();
        for (ProductForm f : forms) {
            Product p = ProductConversion.convertFormToPojo(f);
            p.setClientId(clientIdByName.get(f.getClientName().trim()));
            products.add(p);
        }

        productApi.addBulk(products);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ProductData> searchWithClientNames(String name, String barcode, String clientName, int page, int size) throws ApiException {

        List<Product> pojos = productApi.search(name, barcode, clientName, page, size);
        Set<Integer> clientIds = pojos.stream()
                .map(Product::getClientId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        final Map<Integer, String> clientNameById =
                clientIds.isEmpty()
                        ? Map.of()
                        : clientApi.getByIds(new ArrayList<>(clientIds)).stream()
                        .collect(Collectors.toMap(
                                Client::getId,
                                Client::getName,
                                (a, b) -> a
                        ));

        List<ProductData> dataList = pojos.stream()
                .map(p -> {
                    String cn = clientNameById.getOrDefault(p.getClientId(), "Unknown Client");
                    return ProductConversion.convertPojoToData(p.getId(), p, cn);
                })
                .toList();

        Long total = productApi.getCount(name, barcode, clientName);
        return PaginatedResponse.of(dataList, total, page);
    }

    @Transactional(readOnly = true)
    public Long getCount(String name, String barcode, String clientName) {
        return productApi.getCount(name, barcode, clientName);
    }

    private Integer getClientIdByName(String clientName) throws ApiException {
        String cn = (clientName == null) ? null : clientName.trim();
        if (cn == null || cn.isEmpty()) throw new ApiException("Client name is required");

        Client client = clientApi.getByName(cn);
        if (client == null) throw new ApiException("Client not found: " + cn);
        return client.getId();
    }
}