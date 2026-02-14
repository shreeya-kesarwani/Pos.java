package com.pos.dto;

import com.pos.api.InventoryApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.flow.InventoryFlow;
import com.pos.model.data.InventoryData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.InventoryForm;
import com.pos.pojo.Inventory;
import com.pos.pojo.Product;
import com.pos.utils.InventoryConversion;
import com.pos.utils.InventoryTsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class InventoryDto extends AbstractDto {

    @Autowired private InventoryFlow inventoryFlow;
    @Autowired private InventoryApi inventoryApi;
    @Autowired private ProductApi productApi;

    public void upload(MultipartFile file) throws ApiException, IOException {

        List<InventoryForm> forms = InventoryTsvParser.parse(file);
        if (CollectionUtils.isEmpty(forms)) return;

        List<String> barcodes = InventoryApi.extractBarcodes(forms);
        List<Product> products = productApi.getCheckByBarcodes(barcodes);
        Map<String, Integer> productIdByBarcode = ProductApi.toProductIdByBarcode(products);
        List<Inventory> inventories = InventoryConversion.convertFormsToPojos(forms, productIdByBarcode);

        inventoryApi.add(inventories);
    }

    public PaginatedResponse<InventoryData> getAll(String barcode, String productName, Integer pageNumber, Integer pageSize) throws ApiException {

        barcode = normalize(barcode);
        productName = normalize(productName);

        List<Inventory> inventories = inventoryFlow.searchInventories(barcode, productName, pageNumber, pageSize);
        long total = inventoryFlow.getSearchCount(barcode, productName);

        List<Integer> pageProductIds = InventoryApi.extractDistinctProductIds(inventories);
        List<Product> products = productApi.getByIds(pageProductIds);

        List<InventoryData> dataList = InventoryConversion.toDataList(inventories, products);
        return PaginatedResponse.of(dataList, total, pageNumber);
    }

}