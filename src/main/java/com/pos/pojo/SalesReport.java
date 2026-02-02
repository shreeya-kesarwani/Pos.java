package com.pos.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalesReport {
    private String barcode;
    private String productName;
    private Integer quantity;
    private Double revenue;
}
