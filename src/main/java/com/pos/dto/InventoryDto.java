package com.pos.dto;

import com.pos.exception.ApiException;
import com.pos.exception.UploadValidationException;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class InventoryDto extends AbstractDto {

    @Autowired
    private InventoryFlow inventoryFlow;

    public void upload(MultipartFile file) throws ApiException, IOException {

        InventoryTsvParser.InventoryTsvParseResult parsed = InventoryTsvParser.parse(file);
        List<Inventory> inventories = parsed.forms().stream()
                .map(InventoryConversion::convertFormToPojo)
                .toList();

        inventoryFlow.upsertBulk(inventories, parsed.barcodes());
    }

    public PaginatedResponse<InventoryData> getAll(String barcode, String productName, String clientName, Integer pageNumber, Integer pageSize) throws ApiException {

        String normalised_barcode = normalize(barcode);
        String normalised_productName = normalize(productName);
        String normalised_clientName = normalize(clientName);

        List<Inventory> inventories = inventoryFlow.search(normalised_barcode, normalised_productName, normalised_clientName, pageNumber, pageSize);

        Set<Integer> productIds = inventories.stream()
                .map(Inventory::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Product> products = inventoryFlow.getProductsByIds(productIds);

        Map<Integer, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b2) -> a));

        List<InventoryData> dataList = inventories.stream()
                .map(inv -> {
                    Product p = productMap.get(inv.getProductId());

                    if (p == null) return null;
                    return InventoryConversion.convertPojoToData(inv, p.getBarcode(), p.getName());
                })
                .filter(Objects::nonNull)
                .toList();

        Long totalCount = inventoryFlow.getCount(normalised_barcode, normalised_productName, normalised_clientName);
        return PaginatedResponse.of(dataList, totalCount, pageNumber);
    }
}
