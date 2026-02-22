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