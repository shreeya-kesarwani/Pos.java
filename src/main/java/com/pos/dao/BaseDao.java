package com.pos.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

public abstract class BaseDao {

    @PersistenceContext
    protected EntityManager entityManager; //

    protected EntityManager em() {
        return entityManager;
    }

    public <T> T select(Integer id, Class<T> clazz) {
        return entityManager.find(clazz, id);
    }

    public <T> void insert(T entity) {
        entityManager.persist(entity);
    }

    public <T> Long count(Class<T> clazz) {
        String jpql = "SELECT count(e) FROM " + clazz.getSimpleName() + " e";
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }

    public <T> List<T> selectAll(Class<T> clazz) {
        String jpql = "SELECT e FROM " + clazz.getSimpleName() + " e";
        return entityManager.createQuery(jpql, clazz).getResultList();
    }
}