package com.pos.dao;

import com.pos.pojo.ClientPojo;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ClientDao extends BaseDao {

    public List<ClientPojo> search(Integer id, String name, String email, int page, int size) {
        CriteriaBuilder cb = em().getCriteriaBuilder();
        CriteriaQuery<ClientPojo> cq = cb.createQuery(ClientPojo.class);
        Root<ClientPojo> root = cq.from(ClientPojo.class);

        // Use the common helper for filtering logic
        cq.where(getPredicates(cb, root, id, name, email));

        TypedQuery<ClientPojo> query = em().createQuery(cq);

        // Task 1: Pagination
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        return query.getResultList();
    }

    public Long getCount(Integer id, String name, String email) {
        CriteriaBuilder cb = em().getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ClientPojo> root = cq.from(ClientPojo.class);

        // Task 2: Use the same common helper for dynamic count
        cq.select(cb.count(root)).where(getPredicates(cb, root, id, name, email));

        return em().createQuery(cq).getSingleResult();
    }

    // Common Helper Method for CriteriaBuilder Logic
    private Predicate[] getPredicates(CriteriaBuilder cb, Root<ClientPojo> root, Integer id, String name, String email) {
        List<Predicate> predicates = new ArrayList<>();

        if (id != null) {
            predicates.add(cb.equal(root.get("id"), id));
        }
        if (name != null && !name.isEmpty()) {
            // Partial match for user-friendly search
            predicates.add(cb.like(root.get("name"), "%" + name + "%"));
        }
        if (email != null && !email.isEmpty()) {
            // Partial match for email search
            predicates.add(cb.like(root.get("email"), "%" + email + "%"));
        }

        return predicates.toArray(new Predicate[0]);
    }
}