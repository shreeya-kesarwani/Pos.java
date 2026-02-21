//package com.pos.api;
//
//import com.pos.dao.UserDao;
//import com.pos.exception.ApiException;
//import com.pos.pojo.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.CollectionUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//import static com.pos.model.constants.ErrorMessages.*;
//
//@Component
//@Transactional(rollbackFor = Exception.class)
//public class UserUploadApi {
//
//    @Autowired
//    private UserDao userDao;
//
//    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//
//    public void bulkCreateOrUpdate(List<User> incomingUsers) throws ApiException {
//        if (CollectionUtils.isEmpty(incomingUsers)) {
//            throw new ApiException(USER_BULK_EMPTY.value());
//        }
//
//        List<String> emails = new ArrayList<>(incomingUsers.size());
//        for (User incoming : incomingUsers) {
//            String email = incoming.getEmail();
//            if (email == null || email.isBlank()) {
//                throw new ApiException(INVALID_EMAIL.value() + ": " + email);
//            }
//            emails.add(email);
//        }
//
//        List<User> existingUsers = userDao.findByEmails(emails);
//
//        Map<String, User> existingByEmail = existingUsers.stream()
//                .filter(u -> u.getEmail() != null)
//                .collect(Collectors.toMap(User::getEmail, u -> u, (a, b) -> a));
//
//        for (User incoming : incomingUsers) {
//            User existing = existingByEmail.get(incoming.getEmail());
//
//            if (existing == null) {
//                incoming.setPassword(encoder.encode(incoming.getPassword()));
//                userDao.insert(incoming);
//            } else {
//                existing.setRole(incoming.getRole());
//            }
//        }
//    }
//}
//
package com.pos.api;

import com.pos.dao.UserDao;
import com.pos.exception.ApiException;
import com.pos.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.pos.model.constants.ErrorMessages.*;

@Component
@Transactional(rollbackFor = Exception.class)
public class UserUploadApi {

    @Autowired
    private UserDao userDao;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public void bulkCreateOrUpdate(List<User> incomingUsers) throws ApiException {

        if (CollectionUtils.isEmpty(incomingUsers)) {
            throw new ApiException(USER_BULK_EMPTY.value());
        }
        for (User incoming : incomingUsers) {
            String email = incoming.getEmail();
            if (email == null || email.isBlank()) {
                throw new ApiException(INVALID_EMAIL.value() + ": " + email);
            }
            User existing = userDao.findByEmail(email).orElse(null);
            if (existing == null) {
                incoming.setPasswordHash(encoder.encode(incoming.getPasswordHash()));
                userDao.insert(incoming);
            } else {
                existing.setRole(incoming.getRole());
            }
        }
    }
}