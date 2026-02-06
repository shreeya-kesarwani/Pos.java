package com.pos.controller;

import com.pos.dto.AuthDto;
import com.pos.exception.ApiException;
import com.pos.model.data.AuthData;
import com.pos.model.form.ChangePasswordForm;
import com.pos.model.form.LoginForm;
import com.pos.model.form.SignupForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

//todo - add cookies, expire JWT Token after 1 hr - explore both the options
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthDto authDto;

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public AuthData signup(@Valid @RequestBody SignupForm form) throws ApiException {
        return authDto.signup(form);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public AuthData login(@Valid @RequestBody LoginForm form) throws ApiException {
        return authDto.login(form);
    }

    @RequestMapping(value = "/change-password", method = RequestMethod.POST)
    public void changePassword(@Valid @RequestBody ChangePasswordForm form)
            throws ApiException {
        authDto.changePassword(form);
    }
}
