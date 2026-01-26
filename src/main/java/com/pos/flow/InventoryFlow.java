package com.pos.flow;

import com.pos.pojo.Inventory;
import com.pos.pojo.Product;
import com.pos.exception.ApiException;
import com.pos.api.ClientApi;
import com.pos.api.InventoryApi;
import com.pos.api.ProductApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(rollbackFor = ApiException.class)
public class InventoryFlow {

    @Autowired private InventoryApi inventoryApi;
    @Autowired private ProductApi productApi;
    @Autowired private ClientApi clientApi; // Renamed from ClientService

    public void upsert(String barcode, Inventory p) throws ApiException {
        Product product = productApi.getCheckByBarcode(barcode);
        p.setProductId(product.getId());

        Inventory existing = inventoryApi.getByProductId(product.getId());

        if (existing == null) {
            inventoryApi.add(p);
        } else {
            existing.setQuantity(existing.getQuantity() + p.getQuantity());
        }
    }

    public void upsertBulk(List<Inventory> pojos, List<String> barcodes) throws ApiException {
        for (int i = 0; i < pojos.size(); i++) {
            upsert(barcodes.get(i), pojos.get(i));
        }
    }

    @Transactional(readOnly = true)
    public String getClientName(Integer productId) throws ApiException {
        Integer clientId = productApi.getCheck(productId).getClientId();
        // Updated to use the new Api naming pattern
        return (clientId == null) ? "N/A" : clientApi.getCheck(clientId).getName();
    }

    @Transactional(readOnly = true)
    public List<Inventory> search(String barcode, String productName, String clientName) {
        return inventoryApi.search(barcode, productName, clientName);
    }

    @Transactional(readOnly = true)
    public String getBarcode(Integer productId) throws ApiException {
        return productApi.getCheck(productId).getBarcode();
    }

    @Transactional(readOnly = true)
    public String getProductName(Integer productId) throws ApiException {
        return productApi.getCheck(productId).getName();
    }
}