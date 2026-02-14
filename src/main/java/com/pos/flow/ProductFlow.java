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
    @Autowired private ProductApi productApi;
    @Autowired private ClientApi clientApi;

    public void add(Product product) throws ApiException {
        clientApi.getCheck(product.getClientId());
        productApi.add(product);
    }

    public void addBulk(List<Product> products, Integer clientId) throws ApiException {
        clientApi.getCheck(clientId);
        productApi.addBulk(products);
    }
}