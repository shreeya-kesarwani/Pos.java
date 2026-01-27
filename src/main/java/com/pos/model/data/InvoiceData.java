package com.pos.model.data;

import lombok.Getter;
import lombok.Setter;
import java.time.ZonedDateTime;

@Getter @Setter
public class InvoiceData {
    private Integer orderId;
    private String base64Pdf;
}

