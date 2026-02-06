package com.pos.model.data;

import com.pos.model.constants.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionData {
    private Integer userId;
    private UserRole role;
}
