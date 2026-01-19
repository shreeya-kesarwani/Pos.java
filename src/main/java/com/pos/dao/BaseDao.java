package com.pos.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public abstract class BaseDao {

    @PersistenceContext
    protected EntityManager entityManager;

    // Service calls this as 'select'
    public <T> T select(Integer id, Class<T> clazz) {
        return entityManager.find(clazz, id);
    }

    // Service calls this as 'selectAll'
    public <T> List<T> selectAll(Class<T> clazz) {
        String jpql = "SELECT e FROM " + clazz.getCanonicalName() + " e";
        TypedQuery<T> query = entityManager.createQuery(jpql, clazz);
        return query.getResultList();
    }

    // Service calls this as 'insert'
    public <T> void insert(T entity) {
        entityManager.persist(entity);
    }

    // Service calls this as 'update'
    public <T> T update(T entity) {
        return entityManager.merge(entity);
    }
}