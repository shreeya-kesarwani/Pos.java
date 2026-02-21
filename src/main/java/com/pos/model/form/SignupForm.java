package com.pos.model.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupForm {

    @NotBlank
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank
    private String password;
}