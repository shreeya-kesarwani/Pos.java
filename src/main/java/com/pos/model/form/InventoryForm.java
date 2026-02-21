package com.pos.model.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryForm {
    @NotBlank
    private String barcode;

    @NotNull
    private Integer quantity;
}