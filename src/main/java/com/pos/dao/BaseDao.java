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

    public <T> T selectById(Integer id, Class<T> clazz) {
        return entityManager.find(clazz, id);
    }

    public <T> List<T> selectAll(Class<T> clazz) {
        String jpql = "SELECT e FROM " + clazz.getCanonicalName() + " e";
        TypedQuery<T> query = entityManager.createQuery(jpql, clazz);
        return query.getResultList();
    }

    // Service calls this as 'insert'
    public <T> void insert(T entity) {
        entityManager.persist(entity);
    }
    //
    public <T> T update(T entity) {
        return entityManager.merge(entity);
    }
    //what is getCanonicalName()?
    public <T> List<T> selectAllPaged(Class<T> clazz, int page, int size) {
        String jpql = "SELECT e FROM " + clazz.getCanonicalName() + " e";
        TypedQuery<T> query = entityManager.createQuery(jpql, clazz);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }
    //make filtered rows also paginated, single filtering function for single/multiple
    protected <T> T getSingle(TypedQuery<T> query) {
        List<T> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    // Missing: Needed for Pagination UI info
    public <T> Long count(Class<T> clazz) {
        String jpql = "SELECT count(e) FROM " + clazz.getCanonicalName() + " e";
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }

    // Missing: Needed for Inline Delete
    public <T> void delete(Integer id, Class<T> clazz) {
        T entity = selectById(id, clazz);
        if (entity != null) {
            entityManager.remove(entity);
        }
    }
}