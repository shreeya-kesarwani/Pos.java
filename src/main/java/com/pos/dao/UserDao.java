package com.pos.dao;

import com.pos.pojo.User;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDao extends BaseDao {

    private static final String FIND_BY_EMAIL_JPQL = "SELECT u FROM User u WHERE u.email = :email";

    public Optional<User> findByEmail(String emailLowercase) {
        TypedQuery<User> q = em().createQuery(FIND_BY_EMAIL_JPQL, User.class);
        q.setParameter("email", emailLowercase);
        return q.getResultStream().findFirst();
    }

    public Optional<User> findById(Integer id) {
        return Optional.ofNullable(select(id, User.class));
    }

    public User save(User user) {
        if (user.getId() == null) {
            insert(user);
            return user;
        }
        return em().merge(user);
    }
}
