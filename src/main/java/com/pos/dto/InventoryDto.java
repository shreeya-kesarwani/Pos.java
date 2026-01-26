package com.pos.dto;

import com.pos.flow.InventoryFlow;
import com.pos.model.data.InventoryData;
import com.pos.model.form.InventoryForm;
import com.pos.pojo.Inventory;
import com.pos.exception.ApiException;
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

        for (InventoryForm f : forms) {
            validateForm(f);
            normalize(f);
            Inventory p = InventoryConversion.convertFormToPojo(f);
            pojoList.add(p);
            barcodes.add(f.getBarcode());
        }
        // Atomic call to Flow
        inventoryFlow.upsertBulk(pojoList, barcodes);
    }

    public List<InventoryData> getAll(String barcode, String productName, String clientName) throws ApiException {
        List<Inventory> pojos = inventoryFlow.search(barcode, productName, clientName);
        List<InventoryData> dataList = new ArrayList<>();

        for (Inventory pojo : pojos) {
            try {
                // If a product was deleted but inventory remains, these calls throw an error
                // We catch it here so only that row is skipped, not the whole list
                String b = inventoryFlow.getBarcode(pojo.getProductId());
                String pName = inventoryFlow.getProductName(pojo.getProductId());
                String cName = inventoryFlow.getClientName(pojo.getProductId());

                dataList.add(InventoryConversion.convertPojoToData(pojo, b, pName, cName));
            } catch (Exception e) {
                // Log and skip orphaned inventory record
                System.out.println("Skipping invalid inventory record for ID: " + pojo.getId());
            }
        }
        return dataList;
    }
}