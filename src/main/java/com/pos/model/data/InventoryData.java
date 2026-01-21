package com.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InventoryData {
    private String barcode;
    private String productName;
    private String clientName;
    private Integer quantity;
}