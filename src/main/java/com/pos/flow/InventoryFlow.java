package com.pos.flow;

import com.pos.pojo.Inventory;
import com.pos.pojo.Product;
import com.pos.exception.ApiException;
import com.pos.service.ClientService;
import com.pos.service.InventoryService;
import com.pos.service.ProductService;
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

    // Inside InventoryFlow.java

    public void addOrUpdate(String barcode, Inventory p) throws ApiException {
        Integer productId = resolveProductId(barcode);
        p.setProductId(productId);

        //use productId in params
        List<Inventory> list = inventoryService.search(barcode, null, null);

        //should be handled by service layer
        //search api should be only for ui filter, internal should be by get and getcheckby
        if (list.isEmpty()) {
            inventoryService.add(p);
        } else {
            Inventory existing = list.get(0);
            existing.setQuantity(existing.getQuantity() + p.getQuantity());
        }
    }

    @Transactional(readOnly = true)
    public List<Inventory> search(String barcode, String productName, String clientName) {
        return inventoryService.search(barcode, productName, clientName);
    }

    // Pattern Match: Matches getClientName(Integer clientId) from ProductFlow
    @Transactional(readOnly = true)
    public String getClientName(Integer productId) throws ApiException {
        Integer clientId = productService.getCheck(productId).getClientId();
        return (clientId == null) ? "N/A" : clientService.getCheckById(clientId).getName();
    }

    // Pattern Match: Matches getBarcode/getProductName logic
    @Transactional(readOnly = true)
    public String getBarcode(Integer productId) throws ApiException {
        return productService.getCheck(productId).getBarcode();
    }

    @Transactional(readOnly = true)
    public String getProductName(Integer productId) throws ApiException {
        return productService.getCheck(productId).getName();
    }

    private Integer resolveProductId(String barcode) throws ApiException {
        // Exact match search
        List<Product> products = productService.search(null, barcode, null, 0, 1);
        if (products.isEmpty()) {
            throw new ApiException(String.format("Product with barcode [%s] does not exist", barcode));
        }
        return products.get(0).getId();
    }
}