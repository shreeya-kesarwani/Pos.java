package com.pos.api;

import com.pos.dao.UserDao;
import com.pos.exception.ApiException;
import com.pos.model.constants.UserRole;
import com.pos.model.data.AuthData;
import com.pos.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.pos.model.constants.ErrorMessages.*;
import static com.pos.security.JwtUtil.createToken;
import static com.pos.utils.AuthConversion.convertSignupToUser;
import static com.pos.utils.AuthConversion.convertUserToLoginData;

@Component
@Transactional(rollbackFor = ApiException.class)
public class AuthApi {

    @Autowired
    private UserDao userDao;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User signup(String email, String password) throws ApiException {

        if (userDao.findByEmail(email).isPresent()) {
            throw new ApiException(EMAIL_ALREADY_REGISTERED.value() + ": " + email);
        }

        String passwordHash = encoder.encode(password);

        User user = convertSignupToUser(email, passwordHash, UserRole.OPERATOR);

        userDao.insert(user);
        return user;
    }

    @Transactional(readOnly = true)
    public AuthData login(String email, String password) throws ApiException {
        String normalizedEmail = normalizeEmail(email);

        User user = userDao.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ApiException(INVALID_CREDENTIALS.value() + ": " + normalizedEmail));

        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new ApiException(INVALID_CREDENTIALS.value());
        }

        String token = createToken(user.getId(), user.getRole());
        return convertUserToLoginData(user, token);
    }

    public void changePassword(Integer userId, String currentPassword, String newPassword)
            throws ApiException {

        User user = userDao.selectById(userId);
        if (user == null) {
            throw new ApiException(USER_NOT_FOUND.value() + ": " + userId);
        }

        if (!encoder.matches(currentPassword, user.getPasswordHash())) {
            throw new ApiException(CURRENT_PASSWORD_INCORRECT.value());
        }

        validatePassword(newPassword);
        user.setPasswordHash(encoder.encode(newPassword));
    }

    private String normalizeEmail(String email) throws ApiException {
        if (email == null || email.trim().isEmpty()) {
            throw new ApiException(EMAIL_CANNOT_BE_EMPTY.value());
        }
        return email.trim().toLowerCase();
    }

    private void validatePassword(String password) throws ApiException {
        if (password == null || password.trim().isEmpty()) {
            throw new ApiException(PASSWORD_CANNOT_BE_EMPTY.value());
        }
    }
}
