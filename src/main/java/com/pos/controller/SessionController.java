package com.pos.controller;

import com.pos.model.data.SessionData;
import com.pos.security.AuthPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class SessionController {

    @GetMapping("/session")
    public SessionData session(@AuthenticationPrincipal AuthPrincipal principal) {

        if (principal == null) {
            return new SessionData();
        }

        SessionData data = new SessionData();
        data.setUserId(principal.getUserId());
        data.setRole(principal.getRole());
        return data;
    }
}
