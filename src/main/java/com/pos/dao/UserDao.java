//package com.pos.dao;
//
//import com.pos.pojo.User;
//import org.springframework.stereotype.Repository;
//import org.springframework.util.CollectionUtils;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public class UserDao extends BaseDao {
//
//    private static final String FIND_BY_EMAIL = "SELECT u FROM User u WHERE u.email = :email";
//    private static final String FIND_BY_EMAILS = "SELECT u FROM User u WHERE u.email IN :emails";
//
//    public Optional<User> findByEmail(String email) {
//        return createQuery(FIND_BY_EMAIL, User.class)
//                .setParameter("email", email)
//                .getResultStream()
//                .findFirst();
//    }
//
//    public List<User> findByEmails(List<String> emails) {
//        if (CollectionUtils.isEmpty(emails)) return List.of();
//
//        return createQuery(FIND_BY_EMAILS, User.class)
//                .setParameter("emails", emails)
//                .getResultList();
//    }
//
//    public User selectById(Integer id) {
//        return select(id, User.class);
//    }
//}

package com.pos.dao;

import com.pos.pojo.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDao extends BaseDao {

    private static final String FIND_BY_EMAIL = "SELECT u FROM User u WHERE u.email = :email";

    public Optional<User> findByEmail(String email) {
        System.out.println("DAO");
        return createQuery(FIND_BY_EMAIL, User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }
    public User selectById(Integer id) {
        return select(id, User.class);
    }
}