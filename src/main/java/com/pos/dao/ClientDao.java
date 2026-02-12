package com.pos.dao;

import com.pos.pojo.Client;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Repository
public class ClientDao extends BaseDao {

    private static final String SEARCH_COUNT_BASE_QUERY = """
            FROM Client c
            WHERE (:id IS NULL OR c.id = :id)
            AND (:name IS NULL OR c.name LIKE :name)
            AND (:email IS NULL OR c.email LIKE :email)""";

    private static final String SEARCH_QUERY = "SELECT c " + SEARCH_COUNT_BASE_QUERY + " ORDER BY c.id";
    private static final String COUNT_QUERY = "SELECT COUNT(c) " + SEARCH_COUNT_BASE_QUERY;
    private static final String SELECT_BY_NAME = "SELECT c FROM Client c WHERE c.name = :name";
    private static final String SELECT_BY_NAMES = "SELECT c FROM Client c WHERE c.name IN :names";
    private static final String SELECT_BY_IDS = "SELECT c FROM Client c WHERE c.id IN :ids";

    public List<Client> searchByParams(Integer id, String name, String email, int page, int size) {

        return createQuery(SEARCH_QUERY, Client.class)
                .setParameter("id", id)
                .setParameter("name", like(name))
                .setParameter("email", like(email))
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }


    public Long getCount(Integer id, String name, String email) {

        return createQuery(COUNT_QUERY, Long.class)
                .setParameter("id", id)
                .setParameter("name", like(name))
                .setParameter("email", like(email))
                .getSingleResult();
    }

    public Client selectByName(String name) {
        if (name == null) return null;

        List<Client> list = createQuery(SELECT_BY_NAME, Client.class)
                .setParameter("name", name)
                .setMaxResults(1)
                .getResultList();

        return list.isEmpty() ? null : list.get(0);
    }

    public List<Client> selectByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) return List.of();

        return createQuery(SELECT_BY_NAMES, Client.class)
                .setParameter("names", names)
                .getResultList();
    }

    public List<Client> selectByIds(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) return List.of();

        return createQuery(SELECT_BY_IDS, Client.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    public Client selectById(Integer id) {
        return select(id, Client.class);
    }
}
