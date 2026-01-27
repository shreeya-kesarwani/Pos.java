package com.pos.flow;

import com.pos.pojo.Client;
import com.pos.pojo.Product;
import com.pos.exception.ApiException;
import com.pos.api.ClientApi;
import com.pos.api.ProductApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(rollbackFor = ApiException.class)
public class ProductFlow {

    @Autowired private ProductApi productApi;
    @Autowired private ClientApi clientApi;

    public void add(Product p, String clientName) throws ApiException {
        p.setClientId(getClientIdByName(clientName));
        productApi.add(p);
    }

    public void update(String barcode, Product p, String clientName) throws ApiException {
        p.setClientId(getClientIdByName(clientName));
        productApi.update(barcode, p);
    }

    @Transactional(readOnly = true)
    public List<Product> search(String name, String barcode, String clientName, int page, int size) {
        return productApi.search(name, barcode, clientName, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(String name, String barcode, String clientName) {
        return productApi.getCount(name, barcode, clientName);
    }

    @Transactional(readOnly = true)
    public String getClientName(Integer clientId) throws ApiException {

        if (clientId == null) {
            throw new ApiException("Client ID is missing for product");
        }

        return clientApi.getCheck(clientId).getName();
    }

    private Integer getClientIdByName(String clientName) throws ApiException {
        Client client = clientApi.getCheckByName(clientName);
        return client.getId();
    }

    public void addBulk(List<Product> products, List<String> clientNames) throws ApiException {
        for (int product_index = 0; product_index < products.size(); product_index++) {
            Product product = products.get(product_index);
            String clientName = clientNames.get(product_index);

            product.setClientId(getClientIdByName(clientName));
            productApi.add(product);
        }
    }
}