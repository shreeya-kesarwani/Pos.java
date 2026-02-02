package com.pos.flow;

import com.pos.api.ClientApi;
import com.pos.api.InventoryApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.pojo.Inventory;
import com.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Transactional(rollbackFor = ApiException.class)
public class InventoryFlow {

    @Autowired
    InventoryApi inventoryApi;
    @Autowired
    ProductApi productApi;
    @Autowired
    ClientApi clientApi;

    public void upsertBulk(List<Inventory> inventories, List<String> barcodes)
            throws ApiException {

        if (inventories.isEmpty()) return;
        if (inventories.size() != barcodes.size()) {
            throw new ApiException("Invalid upload: barcode list size mismatch");
        }

        List<String> uniqueBarcodes = barcodes.stream()
                .distinct()
                .toList();
        List<Product> products = productApi.getCheckByBarcodes(uniqueBarcodes);

        Map<String, Integer> productIdByBarcode = products.stream()
                .collect(Collectors.toMap(
                        Product::getBarcode,
                        Product::getId,
                        (a, b) -> a
                ));

        for (int i = 0; i < inventories.size(); i++) {
            Inventory inv = inventories.get(i);
            String barcode = barcodes.get(i);

            Integer productId = productIdByBarcode.get(barcode);
            if (productId == null) {
                throw new ApiException("Product not found for barcode: " + barcode);
            }
            inv.setProductId(productId);
            Inventory existing = inventoryApi.getByProductId(productId);
            if (existing == null) {
                inventoryApi.add(inv);
            } else {
                existing.setQuantity(inv.getQuantity());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Inventory> search(String barcode, String productName, String clientName, int page, int size) throws ApiException {

        if (clientName != null) {
            clientApi.getCheckByName(clientName);
        }
        return inventoryApi.search(barcode, productName, clientName, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(String barcode, String productName, String clientName) throws ApiException {
        if (clientName != null) {
            clientApi.getCheckByName(clientName);
        }
        return inventoryApi.getCount(barcode, productName, clientName);
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByIds(Set<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) return List.of();
        return productApi.getByIds(new ArrayList<>(productIds));
    }
}
