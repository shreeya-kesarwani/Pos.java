package com.pos.model.form;

import jakarta.validation.constraints.Email;
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
    @NotBlank(message = "Email cannot be empty")
//    @Email(message = "Please provide a valid email address")
    private String clientName;
    @NotNull
    private String name;
    @NotNull
    private Double mrp;
    @URL(message = "Please provide a valid URL")
    private String imageUrl;
}