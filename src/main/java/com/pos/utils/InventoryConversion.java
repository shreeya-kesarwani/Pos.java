package com.pos.utils;

import com.pos.model.data.InventoryData;
import com.pos.model.form.InventoryForm;
import com.pos.pojo.Inventory;

public class InventoryConversion {

    public static Inventory convertFormToPojo(InventoryForm form) {
        Inventory p = new Inventory();
        p.setQuantity(form.getQuantity());
        return p;
    }

    public static InventoryData convertPojoToData(Inventory p, String barcode, String productName, String clientName) {
        InventoryData d = new InventoryData();
        d.setQuantity(p.getQuantity());
        d.setBarcode(barcode);
        d.setProductName(productName);
        return d;
    }
}