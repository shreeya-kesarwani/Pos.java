package com.pos.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderItemForm {

    @NotBlank
    private String barcode;

    @NotNull
    @Positive
    private Double sellingPrice;

    @NotNull
    @Positive
    private Integer quantity;
}
