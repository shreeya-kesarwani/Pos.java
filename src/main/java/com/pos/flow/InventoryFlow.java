package com.pos.flow;

import com.pos.api.InventoryApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.pojo.Inventory;
import com.pos.pojo.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Transactional(rollbackFor = ApiException.class)
public class InventoryFlow {

    @Autowired private InventoryApi inventoryApi;
    @Autowired private ProductApi productApi;

    public void upsertBulk(List<Inventory> inventories, List<String> barcodes) throws ApiException {

        List<Product> products = productApi.getCheckByBarcodes(barcodes);

        Map<String, Integer> productIdByBarcode = products.stream()
                .collect(Collectors.toMap(Product::getBarcode, Product::getId, (a, b) -> a));

        inventoryApi.add(inventories, barcodes, productIdByBarcode);
    }
}
