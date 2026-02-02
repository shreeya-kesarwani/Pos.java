package com.pos.api;

import com.pos.dao.UserDao;
import com.pos.exception.ApiException;
import com.pos.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(rollbackFor = ApiException.class)
public class UserApi {

    @Autowired
    private UserDao userDao;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public void bulkCreateOrUpdate(List<User> incomingUsers) throws ApiException {
        for (User incoming : incomingUsers) {
            String email = incoming.getEmail();
            User existing = userDao.findByEmail(email).orElse(null);

            if (existing == null) {
                incoming.setPasswordHash(encoder.encode(incoming.getPasswordHash())); // DTO stores raw password here
                userDao.save(incoming);
            } else {
                existing.setRole(incoming.getRole());
                userDao.save(existing);
            }
        }
    }
}
