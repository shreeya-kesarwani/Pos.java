package com.pos.dto;

import com.pos.model.form.InventoryForm;
import com.pos.pojo.ProductPojo;
import com.pos.service.ApiException;
import com.pos.flow.InventoryFlow; // Ensure this import is correct
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class InventoryDto extends AbstractDto {

    @Autowired private InventoryFlow inventoryFlow;

    public void upload(List<InventoryForm> forms) throws ApiException {
        for (InventoryForm f : forms) {
            // Use the shared validation logic
            validatePositive(f.getQuantity(), "Quantity for barcode: " + f.getBarcode());

            ProductPojo product = inventoryFlow.getProductByBarcode(normalize(f.getBarcode()));
            inventoryFlow.updateInventory(product.getId(), f.getQuantity());
        }
    }
}