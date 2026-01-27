package com.pos.model.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter
public class OrderForm {

    @NotNull
    @Size(min = 1)
    @Valid
    private List<OrderItemForm> items;
}
