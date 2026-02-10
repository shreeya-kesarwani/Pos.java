package com.pos.flow;

import com.pos.api.InventoryApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.model.data.InventoryData;
import com.pos.model.data.PaginatedResponse;
import com.pos.pojo.Inventory;
import com.pos.pojo.Product;
import com.pos.utils.InventoryConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    @Transactional(readOnly = true)
    public PaginatedResponse<InventoryData> searchInventoryData(
            String barcode,
            String productName,
            Integer pageNumber,
            Integer pageSize
    ) throws ApiException {

        List<Inventory> inventories = inventoryApi.search(barcode, productName, pageNumber, pageSize);

        List<Integer> productIds = inventories.stream()
                .map(Inventory::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<Product> products = productIds.isEmpty()
                ? List.of()
                : productApi.getByIds(productIds);

        List<InventoryData> dataList = InventoryConversion.toDataList(inventories, products);

        Long totalCount = inventoryApi.getCount(barcode, productName);
        return PaginatedResponse.of(dataList, totalCount, pageNumber);
    }
}
