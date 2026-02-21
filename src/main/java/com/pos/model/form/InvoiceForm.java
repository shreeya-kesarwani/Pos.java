package com.pos.model.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InvoiceForm {

    @NotNull
    private Integer orderId;

    @NotNull
    private List<InvoiceItemForm> items;
}