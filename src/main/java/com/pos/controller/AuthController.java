package com.pos.controller;

import com.pos.dto.AuthDto;
import com.pos.exception.ApiException;
import com.pos.model.data.AuthData;
import com.pos.model.form.ChangePasswordForm;
import com.pos.model.form.LoginForm;
import com.pos.model.form.SignupForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthDto authDto;

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public AuthData signup(@RequestBody SignupForm form) throws ApiException {
        return authDto.signup(form);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public AuthData login(@RequestBody LoginForm form) throws ApiException {
        return authDto.login(form);
    }

    @RequestMapping(value = "/change-password", method = RequestMethod.POST)
    public void changePassword(@RequestBody ChangePasswordForm form)
            throws ApiException {
        authDto.changePassword(form);
    }
}
