package com.pos.dao;

import com.pos.pojo.ClientPojo;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class ClientDao extends BaseDao {

    public void insert(ClientPojo p) {
        super.insert(p);
    }

    public List<ClientPojo> selectAll() {
        return super.selectAll(ClientPojo.class);
    }

    public ClientPojo select(Integer id) {
        return super.select(id, ClientPojo.class);
    }

    public Optional<ClientPojo> findByEmail(String email) {
        String jpql = "select p from ClientPojo p where email=:email";
        TypedQuery<ClientPojo> query = entityManager.createQuery(jpql, ClientPojo.class);
        query.setParameter("email", email);
        return query.getResultList().stream().findFirst();
    }
}