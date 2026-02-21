package com.pos.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceItemForm {

    @NotBlank
    private String name;

    @NotNull
    @Positive
    private Integer quantity;

    @NotNull
    @Positive
    private Double sellingPrice;
}