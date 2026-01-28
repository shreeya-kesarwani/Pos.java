package com.pos.model.data;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SalesReportData {

    private String barcode;
    private String productName;
    private Integer quantity;
    private BigDecimal revenue;
}
