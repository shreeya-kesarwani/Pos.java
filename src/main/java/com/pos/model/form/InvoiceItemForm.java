package com.pos.model.form;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InvoiceItemForm {
    private String name;
    private Integer quantity;
    private Double sellingPrice;
}