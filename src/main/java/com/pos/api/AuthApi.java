package com.pos.api;

import com.pos.dao.UserDao;
import com.pos.exception.ApiException;
import com.pos.model.data.AuthData;
import com.pos.model.form.ChangePasswordForm;
import com.pos.model.form.LoginForm;
import com.pos.model.form.SignupForm;
import com.pos.pojo.User;
import com.pos.pojo.UserRole;
import com.pos.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(rollbackFor = ApiException.class)
public class AuthApi {

    @Autowired private UserDao userDao;
    @Autowired private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User signup(SignupForm form) throws ApiException {
        String email = normalizeEmail(form.getEmail());
        validatePassword(form.getPassword());

        if (userDao.findByEmail(email).isPresent()) {
            throw new ApiException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(form.getPassword()));
        user.setRole(UserRole.OPERATOR); // ✅ default on self-signup

        userDao.save(user);
        return user;
    }


    @Transactional(readOnly = true)
    public AuthData login(LoginForm form) throws ApiException {
        String email = normalizeEmail(form.getEmail());

        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new ApiException("Invalid credentials"));

        if (!encoder.matches(form.getPassword(), user.getPasswordHash())) {
            throw new ApiException("Invalid credentials");
        }

        String token = jwtUtil.createToken(user.getId(), user.getRole());

        AuthData data = new AuthData();
        data.setToken(token);
        data.setUserId(user.getId());
        data.setRole(user.getRole());
        return data;
    }

    public void changePassword(Integer userId, ChangePasswordForm form) throws ApiException {

        User user = userDao.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        if (!encoder.matches(form.getCurrentPassword(), user.getPasswordHash())) {
            throw new ApiException("Current password is incorrect");
        }

        validatePassword(form.getNewPassword());

        user.setPasswordHash(encoder.encode(form.getNewPassword()));
        userDao.save(user);
    }


    private String normalizeEmail(String email) throws ApiException {
        if (email == null || email.trim().isEmpty()) {
            throw new ApiException("Email cannot be empty");
        }
        return email.trim().toLowerCase();
    }

    private void validatePassword(String password) throws ApiException {
        if (password == null || password.trim().isEmpty()) {
            throw new ApiException("Password cannot be empty");
        }
    }
}
