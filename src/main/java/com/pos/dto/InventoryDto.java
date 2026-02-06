package com.pos.dto;

import com.pos.api.InventoryApi;
import com.pos.exception.ApiException;
import com.pos.flow.InventoryFlow;
import com.pos.model.data.InventoryData;
import com.pos.model.data.PaginatedResponse;
import com.pos.pojo.Inventory;
import com.pos.pojo.Product;
import com.pos.utils.InventoryConversion;
import com.pos.utils.InventoryTsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.pos.model.constants.ErrorMessages.INVALID_UPLOAD_BARCODE_MISMATCH; // <-- add in enum

@Component
public class InventoryDto extends AbstractDto {

    @Autowired private InventoryFlow inventoryFlow;
    @Autowired private InventoryApi inventoryApi;

    public void upload(MultipartFile file) throws ApiException, IOException {

        InventoryTsvParser.InventoryTsvParseResult parsed = InventoryTsvParser.parse(file);
        List<Inventory> inventories = InventoryConversion.convertFormsToPojos(parsed.forms());

        if (inventories.isEmpty()) return;
        if (inventories.size() != parsed.barcodes().size()) {
            throw new ApiException(
                    INVALID_UPLOAD_BARCODE_MISMATCH.value()
                            + " | inventories=" + inventories.size()
                            + ", barcodes=" + parsed.barcodes().size()
            );
        }

        inventoryFlow.upsertBulk(inventories, parsed.barcodes());
    }

    public PaginatedResponse<InventoryData> getAll(
            String barcode,
            String productName,
            Integer pageNumber,
            Integer pageSize
    ) throws ApiException {

        String normalizedBarcode = normalize(barcode);
        String normalizedProductName = normalize(productName);

        List<Inventory> inventories = inventoryApi.search(
                normalizedBarcode, normalizedProductName, pageNumber, pageSize
        );

        Set<Integer> productIds = inventories.stream()
                .map(Inventory::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Keeping your current behavior exactly (you had products = List.of())
        List<Product> products = List.of();

        List<InventoryData> dataList = InventoryConversion.toDataList(inventories, products);

        Long totalCount = inventoryApi.getCount(normalizedBarcode, normalizedProductName);
        return PaginatedResponse.of(dataList, totalCount, pageNumber);
    }
}
