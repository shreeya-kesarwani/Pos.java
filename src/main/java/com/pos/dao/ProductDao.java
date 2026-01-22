package com.pos.dao;

import com.pos.pojo.ClientPojo;
import com.pos.pojo.ProductPojo;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ProductDao extends BaseDao {

    public List<ProductPojo> search(String name, String barcode, String clientName, int page, int size) {
        CriteriaBuilder cb = em().getCriteriaBuilder();
        CriteriaQuery<ProductPojo> cq = cb.createQuery(ProductPojo.class);
        Root<ProductPojo> product = cq.from(ProductPojo.class);

        List<Predicate> predicates = getPredicates(cb, cq, product, name, barcode, clientName);
        cq.where(predicates.toArray(new Predicate[0]));

        TypedQuery<ProductPojo> query = em().createQuery(cq);
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        return query.getResultList();
    }

    public Long getCount(String name, String barcode, String clientName) {
        CriteriaBuilder cb = em().getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ProductPojo> product = cq.from(ProductPojo.class);

        List<Predicate> predicates = getPredicates(cb, cq, product, name, barcode, clientName);
        cq.select(cb.count(product)).where(predicates.toArray(new Predicate[0]));

        return em().createQuery(cq).getSingleResult();
    }

    private List<Predicate> getPredicates(CriteriaBuilder cb, CriteriaQuery<?> cq, Root<ProductPojo> product, String n, String b, String cName) {
        List<Predicate> predicates = new ArrayList<>();

        // 1. Name Filter
        if (n != null && !n.isEmpty()) {
            predicates.add(cb.like(product.get("name"), "%" + n + "%"));
        }

        // 2. Barcode Filter
        if (b != null && !b.isEmpty()) {
            predicates.add(cb.equal(product.get("barcode"), b));
        }

        // 3. Theta-Join Logic
        if (cName != null && !cName.isEmpty()) {
            // We only add the Client table to the query IF we are searching by name
            Root<ClientPojo> client = cq.from(ClientPojo.class);

            // Link them
            predicates.add(cb.equal(product.get("clientId"), client.get("id")));

            // Match the name (using equal for exact match is faster and safer for IDs)
            predicates.add(cb.equal(client.get("name"), cName));
        }

        return predicates;
    }
}