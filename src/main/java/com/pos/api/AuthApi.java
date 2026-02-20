package com.pos.api;

import com.pos.dao.UserDao;
import com.pos.exception.ApiException;
import com.pos.model.constants.UserRole;
import com.pos.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.pos.model.constants.ErrorMessages.*;
import static com.pos.utils.AuthConversion.convertSignupToUser;

@Component
@Transactional(rollbackFor = Exception.class)
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

    public User validateLogin(String email, String password) throws ApiException {

        User user = userDao.findByEmail(email).orElseThrow(() -> new ApiException(INVALID_CREDENTIALS.value() + ": " + email));
        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new ApiException(INVALID_CREDENTIALS.value());
        }
        return user;
    }

    public User getById(Integer userId) throws ApiException {
        User user = userDao.selectById(userId);
        if (user == null) {
            throw new ApiException(USER_NOT_FOUND.value() + ": " + userId);
        }
        return user;
    }

    public void changePassword(Integer userId, String currentPassword, String newPassword) throws ApiException {
        User user = userDao.selectById(userId);
        if (user == null) {
            throw new ApiException(USER_NOT_FOUND.value() + ": " + userId);
        }
        if (!encoder.matches(currentPassword, user.getPasswordHash())) {
            throw new ApiException(CURRENT_PASSWORD_INCORRECT.value());
        }

        user.setPasswordHash(encoder.encode(newPassword));
    }
}
