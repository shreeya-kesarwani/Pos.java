package com.pos.model.form;

import io.micrometer.common.lang.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class ProductForm {
    @NotNull
    private String barcode;

    @NotBlank
    private String clientName;

    @NotNull
    private String name;

    @Min(value = 0)
    @NotNull
    private Double mrp;

    @URL(message = "Please provide a valid URL")
    private String imageUrl;
}