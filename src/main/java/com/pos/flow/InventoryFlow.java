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

import static com.pos.model.constants.ErrorMessages.*;

@Component
@Transactional(rollbackFor = Exception.class)
public class InventoryFlow {

    @Autowired private InventoryApi inventoryApi;
    @Autowired private ProductApi productApi;

    public void upsertBulk(List<Inventory> inventories, List<String> barcodes) throws ApiException {
        List<Product> products = productApi.getCheckByBarcodes(barcodes);

        //todo - make a static method in api to reuse it
        Map<String, Integer> barcodeByProductId = products.stream()
                .collect(Collectors.toMap(Product::getBarcode, Product::getId, (a, b) -> a));

        setProductIds(inventories, barcodes, barcodeByProductId);
        inventoryApi.add(inventories);
    }

    @Transactional(readOnly = true)
    public InventorySearchResult searchInventory(String barcode, String productName, Integer pageNumber, Integer pageSize) throws ApiException {
//todo reuse the filter for product client id from ui
        boolean hasBarcodeFilter = barcode != null && !barcode.isBlank();
        boolean hasNameFilter = productName != null && !productName.isBlank();
        boolean hasAnyFilter = hasBarcodeFilter || hasNameFilter;

        List<Integer> productIds = productApi.findProductIdsByBarcodeOrName(barcode, productName);
//todo - need of has any filter
        if (hasAnyFilter && productIds.isEmpty()) {
            return new InventorySearchResult(List.of(), List.of(), 0L);
        }

        List<Inventory> inventories = inventoryApi.findByProductIds(productIds, pageNumber, pageSize);
        long totalCount = inventoryApi.getCountByProductIds(productIds);
        List<Product> products = productIds.isEmpty()
                ? List.of()
                : productApi.getByIds(productIds);

        return new InventorySearchResult(inventories, products, totalCount);
    }

    private void setProductIds(List<Inventory> inventories, List<String> barcodes, Map<String, Integer> productIdByBarcode) throws ApiException {
        for (int i = 0; i < inventories.size(); i++) {
            String barcode = barcodes.get(i);
            Integer productId = productIdByBarcode.get(barcode);
            if (productId == null) {
                throw new ApiException(PRODUCT_NOT_FOUND_FOR_BARCODE.value() + ": " + barcode);
            }
            inventories.get(i).setProductId(productId);
        }
    }
//todo make a modal class
    public record InventorySearchResult(
            List<Inventory> inventories,
            List<Product> products,
            long total
    ){}
}
