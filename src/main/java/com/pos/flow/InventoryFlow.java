package com.pos.flow;

import com.pos.pojo.InventoryPojo;
import com.pos.pojo.ProductPojo;
import com.pos.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
@Transactional(rollbackFor = ApiException.class)
public class InventoryFlow {

    @Autowired private InventoryService inventoryService;
    @Autowired private ProductService productService;
    @Autowired private ClientService clientService;

    public ProductPojo getProductByBarcode(String barcode) throws ApiException {
        return productService.getByBarcode(barcode);
    }

    public void updateInventory(Integer productId, Integer quantity) throws ApiException {
        inventoryService.update(productId, quantity);
    }

    @Transactional(readOnly = true)
    public List<InventoryPojo> getPaged(int page, int size) {
        return inventoryService.getPaged(page, size);
    }

    @Transactional(readOnly = true)
    public List<InventoryPojo> search(String barcode, String productName, String clientName) {
        return inventoryService.search(barcode, productName, clientName);
    }

    public String getBarcode(Integer productId) throws ApiException {
        return productService.getCheck(productId).getBarcode();
    }

    public String getProductName(Integer productId) throws ApiException {
        return productService.getCheck(productId).getName();
    }

    public String getClientName(Integer productId) throws ApiException {
        Integer clientId = productService.getCheck(productId).getClientId();
        return clientService.getCheckById(clientId);
    }
}