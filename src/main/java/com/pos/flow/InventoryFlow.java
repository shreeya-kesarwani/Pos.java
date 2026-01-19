package com.pos.flow;

import com.pos.pojo.ProductPojo;
import com.pos.service.ApiException;
import com.pos.service.InventoryService;
import com.pos.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InventoryFlow {

    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private ProductService productService;

    // Added throws ApiException because productService.getByBarcode throws it
    public ProductPojo getProductByBarcode(String barcode) throws ApiException {
        return productService.getByBarcode(barcode);
    }

    // Added @Transactional and throws ApiException
    @Transactional(rollbackFor = ApiException.class)
    public void updateInventory(Integer productId, Integer quantity) throws ApiException {
        inventoryService.update(productId, quantity);
    }
}