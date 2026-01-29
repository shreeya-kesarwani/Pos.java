package com.pos.model.data;

import com.pos.pojo.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthData {
    private String token;
    private Integer userId;
    private UserRole role;
}
