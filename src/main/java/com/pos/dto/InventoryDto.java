package com.pos.dto;

import com.pos.exception.ApiException;
import com.pos.flow.InventoryFlow;
import com.pos.model.data.InventoryData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.InventoryForm;
import com.pos.pojo.Inventory;
import com.pos.utils.InventoryConversion;
import com.pos.utils.TsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class InventoryDto extends AbstractDto {

    @Autowired
    private InventoryFlow inventoryFlow;

    public void upload(MultipartFile file) throws ApiException, IOException {
        List<InventoryForm> forms = TsvParser.parseInventoryTsv(file.getInputStream());

        List<Inventory> pojoList = new ArrayList<>();
        List<String> barcodes = new ArrayList<>();

        for (InventoryForm form : forms) {
            validateForm(form);
            normalize(form);

            Inventory pojo = InventoryConversion.convertFormToPojo(form);
            pojoList.add(pojo);
            barcodes.add(form.getBarcode());
        }

        inventoryFlow.upsertBulk(pojoList, barcodes);
    }

    public PaginatedResponse<InventoryData> getAll(
            String barcode,
            String productName,
            String clientName,
            Integer page,
            Integer size
    ) throws ApiException {

        int pageNumber = (page == null) ? 0 : page;
        int pageSize = (size == null) ? 10 : size;

        String b = normalize(barcode);
        String pn = normalize(productName);
        String cn = normalize(clientName);

        List<Inventory> inventories = inventoryFlow.search(b, pn, cn, pageNumber, pageSize);

        List<InventoryData> dataList = new ArrayList<>();
        for (Inventory inventory : inventories) {
            try {
                String invBarcode = inventoryFlow.getBarcode(inventory.getProductId());
                String invProductName = inventoryFlow.getProductName(inventory.getProductId());

                dataList.add(
                        InventoryConversion.convertPojoToData(inventory, invBarcode, invProductName)
                );
            } catch (Exception e) {
                System.out.println("Skipping invalid inventory record for ID: " + inventory.getId());
            }
        }

        Long totalCount = inventoryFlow.getCount(b, pn, cn);
        return PaginatedResponse.of(dataList, totalCount, pageNumber);
    }
}
