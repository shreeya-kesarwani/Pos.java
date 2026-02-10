package com.pos.flow;

import com.pos.api.ClientApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.pojo.Client;
import com.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    public Map<String, Object> searchProducts(String name,
                                                     String barcode,
                                                     String clientName,
                                                     Integer pageNumber,
                                                     Integer pageSize) throws ApiException {

        List<Product> products = productApi.search(name, barcode, clientName, pageNumber, pageSize);
        long total = productApi.getCount(name, barcode, clientName);

        Set<Integer> clientIds = new HashSet<>();
        for (Product p : products) {
            if (p.getClientId() != null) clientIds.add(p.getClientId());
        }

        Map<Integer, String> clientNameById = Map.of();
        if (!clientIds.isEmpty()) {
            List<Client> clients = clientApi.getByIds(new ArrayList<>(clientIds));
            Map<Integer, String> map = new HashMap<>();
            for (Client c : clients) {
                if (c.getId() != null && c.getName() != null) {
                    map.putIfAbsent(c.getId(), c.getName());
                }
            }
            clientNameById = map;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("total", total);
        result.put("clientNameById", clientNameById);
        return result;
    }
}
