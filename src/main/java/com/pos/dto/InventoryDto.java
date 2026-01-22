package com.pos.dto;

import com.pos.flow.InventoryFlow;
import com.pos.model.data.InventoryData;
import com.pos.model.form.InventoryForm;
import com.pos.pojo.InventoryPojo;
import com.pos.service.ApiException;
import com.pos.utils.InventoryConversion; // Import your new converter
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InventoryDto extends AbstractDto {

    @Autowired
    private InventoryFlow inventoryFlow;

    public void upload(List<InventoryForm> forms) throws ApiException {
        for (InventoryForm f : forms) {
            validateForm(f);
            normalize(f);
            InventoryPojo p = InventoryConversion.convertFormToPojo(f);
            inventoryFlow.addOrUpdate(f.getBarcode(), p);
        }
    }

    public List<InventoryData> getAll(String barcode, String productName, String clientName) throws ApiException {
        List<InventoryPojo> pojos = inventoryFlow.search(barcode, productName, clientName);
        List<InventoryData> dataList = new ArrayList<>();

        for (InventoryPojo pojo : pojos) {
            // CHANGE 2: Get the metadata needed for the conversion via Flow
            String b = inventoryFlow.getBarcode(pojo.getProductId());
            String pName = inventoryFlow.getProductName(pojo.getProductId());
            String cName = inventoryFlow.getClientName(pojo.getProductId());

            // CHANGE 3: Use the converter to create the Data object
            InventoryData data = InventoryConversion.convertPojoToData(pojo, b, pName, cName);
            dataList.add(data);
        }
        return dataList;
    }
}