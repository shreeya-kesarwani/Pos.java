package com.pos.dao;

import com.pos.pojo.ProductPojo;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProductDao extends BaseDao {

    public List<ProductPojo> search(String name, String barcode, Integer clientId) {
        // 1. Simplified Query: No need to join ClientPojo if we only filter by clientId (Integer)
        StringBuilder jpql = new StringBuilder("select p from ProductPojo p where 1=1 ");

        if (name != null && !name.isEmpty()) jpql.append("and p.name like :name ");
        if (barcode != null && !barcode.isEmpty()) jpql.append("and p.barcode like :barcode ");
        if (clientId != null) jpql.append("and p.clientId = :clientId ");

        TypedQuery<ProductPojo> query = entityManager.createQuery(jpql.toString(), ProductPojo.class);

        // 2. Set Parameters safely
        if (name != null && !name.isEmpty()) query.setParameter("name", "%" + name + "%");
        if (barcode != null && !barcode.isEmpty()) query.setParameter("barcode", "%" + barcode + "%");
        if (clientId != null) query.setParameter("clientId", clientId);

        return query.getResultList();
    }

    public ProductPojo selectByBarcode(String barcode) {
        String jpql = "select p from ProductPojo p where p.barcode = :barcode";
        TypedQuery<ProductPojo> query = entityManager.createQuery(jpql, ProductPojo.class);
        query.setParameter("barcode", barcode);
        return getSingle(query);
    }
}