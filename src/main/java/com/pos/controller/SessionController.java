package com.pos.controller;

import com.pos.model.data.SessionData;
import com.pos.security.AuthPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class SessionController {

    @RequestMapping(value = "/session", method = RequestMethod.GET)
    public SessionData session(Authentication authentication) {

        if (authentication == null || authentication.getPrincipal() == null) {
            return new SessionData();
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof AuthPrincipal p)) {
            return new SessionData();
        }

        SessionData data = new SessionData();
        data.setUserId(p.getUserId());
        data.setRole(p.getRole());
        return data;
    }
}
