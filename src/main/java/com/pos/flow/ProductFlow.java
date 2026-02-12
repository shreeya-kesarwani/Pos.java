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

@Component
@Transactional(rollbackFor = Exception.class)
public class ProductFlow {
//todo refactor this file
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

    public void addBulk(List<Product> products, Integer clientId) throws ApiException {
        if (products == null || products.isEmpty()) return;
        if (clientId == null) {
            throw new ApiException("clientId is required for bulk upload");
        }
        clientApi.getCheck(clientId);
        for (Product p : products) {
            p.setClientId(clientId);
        }
        productApi.addBulk(products);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> searchProducts(String name, String barcode, String clientName, Integer pageNumber, Integer pageSize) throws ApiException {
//todo create a model class
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
                    map.put(c.getId(), c.getName());
                }
            }
            clientNameById = map;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("total", total);
        result.put("clientNameById", clientNameById);//todo do this in ui, ui should handle this
        return result;
    }
}