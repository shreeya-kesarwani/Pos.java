package com.pos.model.data;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InvoiceData {
    private Integer orderId;        // To link the PDF back to the Order
    private String base64Pdf;       // The encoded PDF content string
}