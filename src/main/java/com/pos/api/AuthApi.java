package com.pos.api;

import com.pos.dao.UserDao;
import com.pos.exception.ApiException;
import com.pos.model.data.AuthData;
import com.pos.pojo.User;
import com.pos.model.constants.UserRole;
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

    public User signup(String email, String password) throws ApiException {
        String normalizedEmail = normalizeEmail(email);
        validatePassword(password);

        if (userDao.findByEmail(normalizedEmail).isPresent()) {
            throw new ApiException("Email already registered");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(encoder.encode(password));
        user.setRole(UserRole.SUPERVISOR);

        userDao.save(user);
        return user;
    }

    @Transactional(readOnly = true)
    public AuthData login(String email, String password) throws ApiException {
        String normalizedEmail = normalizeEmail(email);

        User user = userDao.findByEmail(normalizedEmail).orElseThrow(() -> new ApiException("Invalid credentials"));

        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new ApiException("Invalid credentials");
        }

        String token = jwtUtil.createToken(user.getId(), user.getRole());

        AuthData data = new AuthData();
        data.setToken(token);
        data.setUserId(user.getId());
        data.setRole(user.getRole());
        return data;
    }

    public void changePassword(Integer userId, String currentPassword, String newPassword) throws ApiException {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        if (!encoder.matches(currentPassword, user.getPasswordHash())) {
            throw new ApiException("Current password is incorrect");
        }

        validatePassword(newPassword);
        user.setPasswordHash(encoder.encode(newPassword));
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
