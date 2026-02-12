package com.pos.dto;

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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public class InventoryDto extends AbstractDto {

    @Autowired private InventoryFlow inventoryFlow;

    public void upload(MultipartFile file) throws ApiException, IOException {

        List<InventoryForm> forms = InventoryTsvParser.parse(file);
        if (forms.isEmpty()) return;

        List<Inventory> inventories = InventoryConversion.convertFormsToPojos(forms);
        if (inventories.isEmpty()) return;

        List<String> barcodes = forms.stream()
                .map(InventoryForm::getBarcode)
                .toList();

        inventoryFlow.upsertBulk(inventories, barcodes);
    }

    public PaginatedResponse<InventoryData> getAll(String barcode, String productName, Integer pageNumber, Integer pageSize) throws ApiException {

        String normalizedBarcode = normalize(barcode);
        String normalizedProductName = normalize(productName);

        InventoryFlow.InventorySearchResult result = inventoryFlow.searchInventory(normalizedBarcode, normalizedProductName, pageNumber, pageSize);
        List<Inventory> inventories = result.inventories();
        List<Product> products = result.products();
        long total = result.total();

        List<InventoryData> dataList = InventoryConversion.toDataList(inventories, products);
        return PaginatedResponse.of(dataList, total, pageNumber);
    }
}