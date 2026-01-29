package com.pos.security;

import com.pos.pojo.UserRole;
import lombok.Getter;

@Getter
public class AuthPrincipal {

    private final Integer userId;
    private final UserRole role;

    public AuthPrincipal(Integer userId, UserRole role) {
        this.userId = userId;
        this.role = role;
    }
}
