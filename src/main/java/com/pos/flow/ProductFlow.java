package com.pos.flow;

import com.pos.api.ClientApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.pojo.Client;
import com.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.pos.model.constants.ErrorMessages.INVALID_CLIENT_NAME_BULK_UPLOAD;

@Component
@Transactional(rollbackFor = ApiException.class)
public class ProductFlow {

    @Autowired private ProductApi productApi;
    @Autowired private ClientApi clientApi;

    public void add(Product product, String clientName) throws ApiException {
        Client client = clientApi.getCheckByName(clientName);
        product.setClientId(client.getId());
        productApi.add(product);
    }

    public void update(String barcode, Product product, String clientName) throws ApiException {
        Client client = clientApi.getCheckByName(clientName);
        product.setClientId(client.getId());
        productApi.update(barcode, product);
    }

    public void addBulk(List<Product> products, List<String> clientNames) throws ApiException {
        if (products == null || products.isEmpty()) return;

        // Resolve all client names -> ids in one go
        List<String> distinctNames = clientNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        List<Client> clients = clientApi.getByNames(distinctNames);

        Map<String, Integer> clientIdByName = clients.stream()
                .collect(Collectors.toMap(Client::getName, Client::getId, (a, b) -> a));

        List<String> missing = distinctNames.stream()
                .filter(n -> !clientIdByName.containsKey(n))
                .toList();

        if (!missing.isEmpty()) {
            throw new ApiException(INVALID_CLIENT_NAME_BULK_UPLOAD.value() + ": " + missing);
        }

        productApi.addBulk(products, clientNames, clientIdByName);
    }
}
