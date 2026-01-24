package com.pos.utils;

import com.pos.model.data.InventoryData;
import com.pos.model.form.InventoryForm;
import com.pos.pojo.Inventory;

public class InventoryConversion {

    // Converts Form to Pojo (Used in Upload/Add)
    public static Inventory convertFormToPojo(InventoryForm form) {
        Inventory p = new Inventory();
        p.setQuantity(form.getQuantity());
        // Note: productId is NOT set here; the Flow handles resolution.
        return p;
    }

    // Converts Pojo to Data (Used in GetAll/Search)
    public static InventoryData convertPojoToData(Inventory p, String barcode, String productName, String clientName) {
        InventoryData d = new InventoryData();
        d.setQuantity(p.getQuantity());
        d.setBarcode(barcode);
        d.setProductName(productName);
//        d.setClientName(clientName);
        return d;
    }
}