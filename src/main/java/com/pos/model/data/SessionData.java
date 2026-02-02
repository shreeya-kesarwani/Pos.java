package com.pos.model.data;

import com.pos.model.constants.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//todo can combine authdata with this maybe, minimise
public class SessionData {
    private Integer userId;
    private UserRole role;
}
