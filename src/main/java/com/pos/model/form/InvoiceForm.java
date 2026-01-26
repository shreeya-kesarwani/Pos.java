package com.pos.model.form;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class InvoiceForm {
    private Integer orderId;
    private List<InvoiceItemForm> items;
}