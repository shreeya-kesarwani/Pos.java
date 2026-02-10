package com.pos.model.data;

import com.pos.model.constants.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthData extends SessionData {
    private String token;
    private String email;
    private UserRole role;
}
