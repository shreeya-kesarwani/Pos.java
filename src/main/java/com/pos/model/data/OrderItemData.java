package com.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderItemData {
    private Integer id;
    private String barcode;
    private String productName;
    private Integer quantity;
    private Double sellingPrice;
}