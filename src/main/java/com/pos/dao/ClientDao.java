package com.pos.dao;

import com.pos.pojo.Client;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClientDao extends BaseDao {

    private static final String BASE_QUERY =
            " FROM Client c " +
                    " WHERE (:id IS NULL OR c.id = :id) " +
                    " AND (:name IS NULL OR c.name LIKE :name) " +
                    " AND (:email IS NULL OR c.email LIKE :email)";

    public List<Client> search(Integer id, String name, String email, int page, int size) {

        String jpql = "SELECT c" + BASE_QUERY + " ORDER BY c.id";

        return em().createQuery(jpql, Client.class)
                .setParameter("id", id)
                .setParameter("name", name)
                .setParameter("email", email)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long getCount(Integer id, String name, String email) {

        String jpql = "SELECT COUNT(c)" + BASE_QUERY;

        return em().createQuery(jpql, Long.class)
                .setParameter("id", id)
                .setParameter("name", name)
                .setParameter("email", email)
                .getSingleResult();
    }

    public List<Client> selectByNames(List<String> names) {
        if (names == null || names.isEmpty()) return List.of();

        String jpql = "SELECT c FROM Client c WHERE c.name IN :names";
        return em().createQuery(jpql, Client.class)
                .setParameter("names", names)
                .getResultList();
    }

    public List<Client> selectByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        String jpql = "SELECT c FROM Client c WHERE c.id IN :ids";
        return em().createQuery(jpql, Client.class)
                .setParameter("ids", ids)
                .getResultList();
    }
}
