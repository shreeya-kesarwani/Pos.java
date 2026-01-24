package com.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderItemData {
    private Integer id;             // OrderItem ID
    private String barcode;         // Resolved from ProductPojo
    private String productName;     // Resolved from ProductPojo
    private Integer quantity;       // Quantity sold
    private Double sellingPrice;    // Price at which it was sold
}