package com.pos.controller;

import com.pos.dto.AuthDto;
import com.pos.exception.ApiException;
import com.pos.model.data.AuthData;
import com.pos.model.form.ChangePasswordForm;
import com.pos.model.form.LoginForm;
import com.pos.model.form.SignupForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    public AuthData signup(@Valid @RequestBody SignupForm form) throws ApiException {
        return authDto.signup(form);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public AuthData login(@Valid @RequestBody LoginForm form, HttpServletRequest request) throws ApiException {
        return authDto.login(form, request);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public void logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @RequestMapping(value = "/change-password", method = RequestMethod.POST)
    public void changePassword(
            @Valid @RequestBody ChangePasswordForm form
    ) throws ApiException {
        authDto.changePassword(form);
    }

    @RequestMapping(value = "/session", method = RequestMethod.GET)
    public AuthData session() throws ApiException {
        return authDto.getSessionInfo();
    }
}
