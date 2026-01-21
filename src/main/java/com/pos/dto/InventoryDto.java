package com.pos.dto;

import com.pos.flow.InventoryFlow;
import com.pos.model.data.InventoryData;
import com.pos.model.form.InventoryForm;
import com.pos.pojo.ProductPojo;
import com.pos.service.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class InventoryDto extends AbstractDto {

    @Autowired private InventoryFlow inventoryFlow;

    public void upload(List<InventoryForm> forms) throws ApiException {
        for (InventoryForm f : forms) {
            validatePositive(f.getQuantity(), "Quantity for barcode: " + f.getBarcode());
            ProductPojo product = inventoryFlow.getProductByBarcode(normalize(f.getBarcode()));
            inventoryFlow.updateInventory(product.getId(), f.getQuantity());
        }
    }

    public List<InventoryData> getAllFiltered(String barcode, String productName, String clientName) throws ApiException {
        return inventoryFlow.search(normalize(barcode), normalize(productName), normalize(clientName)).stream()
                .map(p -> {
                    InventoryData d = new InventoryData();
                    try {
                        d.setBarcode(inventoryFlow.getBarcode(p.getProductId()));
                        d.setProductName(inventoryFlow.getProductName(p.getProductId()));
                        d.setClientName(inventoryFlow.getClientName(p.getProductId()));
                        d.setQuantity(p.getQuantity());
                    } catch (ApiException e) { /* Log metadata error */ }
                    return d;
                }).toList();
    }
}