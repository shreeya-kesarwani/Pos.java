package com.pos.dao;

import com.pos.pojo.User;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDao extends BaseDao {

    public Optional<User> findByEmail(String emailLowercase) {
        if (emailLowercase == null) return Optional.empty();

        TypedQuery<User> q = em().createQuery(
                "select u from User u where u.email = :email",
                User.class
        );
        q.setParameter("email", emailLowercase);

        return q.getResultStream().findFirst();
    }

    public Optional<User> findById(Integer id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(super.select(id, User.class));
    }

    public User save(User user) {
        if (user == null) return null;

        // If id is null => new entity, persist
        if (user.getId() == null) {
            super.insert(user);
            return user;
        }

        // If id exists => update entity
        return em().merge(user);
    }
}
