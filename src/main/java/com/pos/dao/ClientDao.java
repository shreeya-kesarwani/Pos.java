package com.pos.dao;

import com.pos.pojo.ClientPojo;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ClientDao extends BaseDao {

    //TODO query for selecting p from clientPojo p where id=:, id not null then set, name not null set
    //one method which will be generalised
    // Specialized: Needed to validate the "Add Client" Modal
    //if id,name,email == null
    public Optional<ClientPojo> selectByEmail(String email) {
        String jpql = "select p from ClientPojo p where email=:email";
        TypedQuery<ClientPojo> query = entityManager.createQuery(jpql, ClientPojo.class);
        query.setParameter("email", email);
        return Optional.ofNullable(getSingle(query));
    }

    // Specialized: Needed for unique name validation
    public List<ClientPojo> selectByName(String name) {
        String jpql = "select p from ClientPojo p where name=:name";
        TypedQuery<ClientPojo> query = entityManager.createQuery(jpql, ClientPojo.class);
        query.setParameter("name", name);
        return query.getResultList();
    }
}