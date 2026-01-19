package com.pos.flow;

import com.pos.model.data.ProductData;
import com.pos.pojo.ProductPojo;
import com.pos.service.ApiException;
import com.pos.service.ClientService;
import com.pos.service.InventoryService;
import com.pos.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ProductFlow {

    @Autowired private ProductService productService;
    @Autowired private ClientService clientService;
//    @Autowired private InventoryService inventoryService;
    //becuase product has not arrived, remove it
    //when qnty = 0, dont delete -> your choice

    @Transactional(rollbackFor = ApiException.class)
    public void add(ProductPojo productPojo, String email) throws ApiException {
        productPojo.setClientId(clientService.getByEmail(email).getId());
        productService.add(productPojo);
    }

    @Transactional(readOnly = true)
    public List<ProductPojo> getAll() throws ApiException {
        // Return raw POJOs directly from the service
        return productService.getAll();
    }

    @Transactional(readOnly = true)
    public List<ProductPojo> getAllFiltered(String n, String b) throws ApiException {
        // Return raw POJOs directly
        return productService.search(n, b);
    }
    //transactional
    public void update(Integer id, ProductPojo productPojo) throws ApiException {
        productService.update(id, productPojo);
    }
    //never use version in business logic
    //transactional ko class level pr use
    //api bolo
    //classes in flow layer, productflowapi
    //service -> clientapi
    //serive -> api

}