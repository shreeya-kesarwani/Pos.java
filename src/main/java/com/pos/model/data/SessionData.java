package com.pos.model.data;

import com.pos.pojo.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionData {
    private Integer userId;
    private UserRole role;
}
