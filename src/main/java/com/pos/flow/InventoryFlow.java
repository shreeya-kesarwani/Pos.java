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
    @Autowired private ClientApi clientApi;

    public void upsert(String barcode, Inventory inventory) throws ApiException {
        Product product = productApi.getCheckByBarcode(barcode);
        inventory.setProductId(product.getId());

        Inventory existing_inventory = inventoryApi.getByProductId(product.getId());

        if (existing_inventory == null) {
            inventoryApi.add(inventory);
        } else {
            existing_inventory.setQuantity(inventory.getQuantity());
        }
    }

    public void upsertBulk(List<Inventory> inventories, List<String> barcodes) throws ApiException {
        for (int inventory_index = 0; inventory_index < inventories.size(); inventory_index++) {
            upsert(barcodes.get(inventory_index), inventories.get(inventory_index));
        }
    }

    @Transactional(readOnly = true)
    public String getBarcode(Integer productId) throws ApiException {
        return productApi.getCheck(productId).getBarcode();
    }

    @Transactional(readOnly = true)
    public String getProductName(Integer productId) throws ApiException {
        return productApi.getCheck(productId).getName();
    }

    @Transactional(readOnly = true)
    public List<Inventory> search(String barcode, String productName, String clientName, int page, int size) {
        return inventoryApi.search(barcode, productName, clientName, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(String barcode, String productName, String clientName) {
        return inventoryApi.getCount(barcode, productName, clientName);
    }

}