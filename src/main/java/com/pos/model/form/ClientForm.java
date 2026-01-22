package com.pos.model.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClientForm {
    @NotBlank
    private String name;
    @NotBlank
    @Email(message = "Please provide a valid email address")
    private String email;
}