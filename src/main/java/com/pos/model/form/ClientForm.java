package com.pos.model.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
//abstract dto me hoga check valid, using reflections
public class ClientForm {
    @NotBlank
    private String name;
    @NotBlank
    @Email(message = "Please provide a valid email address") //--optional to have, know the implemention
    private String email;
}