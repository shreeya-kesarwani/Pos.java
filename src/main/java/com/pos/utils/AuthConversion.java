package com.pos.utils;

import com.pos.model.constants.UserRole;
import com.pos.model.data.AuthData;
import com.pos.pojo.User;

public class AuthConversion {

    private AuthConversion() {}

    public static User convertSignupToUser(String email, String passwordHash, UserRole role) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        return user;
    }

    public static AuthData convertUserToSignupData(User user) {
        return convertUserToAuthData(user);
    }

    public static AuthData convertUserToLoginData(User user, String token) {
        AuthData data = new AuthData();
        data.setToken(token);
        data.setUserId(user.getId());
        data.setRole(user.getRole());
        data.setEmail(user.getEmail());
        return data;
    }

    public static AuthData convertUserToAuthData(User user) {
        AuthData data = new AuthData();
        data.setUserId(user.getId());
        data.setRole(user.getRole());
        data.setEmail(user.getEmail());
        data.setToken(null);
        return data;
    }

}
