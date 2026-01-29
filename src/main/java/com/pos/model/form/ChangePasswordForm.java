package com.pos.model.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordForm {

    @NotBlank
    private String currentPassword;

    @NotBlank
    private String newPassword;
}
