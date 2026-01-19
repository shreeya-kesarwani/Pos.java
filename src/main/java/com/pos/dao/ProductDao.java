package com.pos.dao;

import com.pos.pojo.ProductPojo;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProductDao extends BaseDao {

    // JPQL query that ignores parameters if they are null
    public List<ProductPojo> search(String name, String barcode) {
        String jpql = "select p from ProductPojo p where " +
                "(:name is null or p.name like :name) and " +
                "(:barcode is null or p.barcode like :barcode)";
        TypedQuery<ProductPojo> query = entityManager.createQuery(jpql, ProductPojo.class);

        // Use % for partial string matching
        query.setParameter("name", name == null ? null : "%" + name + "%");
        query.setParameter("barcode", barcode == null ? null : "%" + barcode + "%");
        return query.getResultList();
    }

    public ProductPojo selectByBarcode(String barcode) {
        String jpql = "select p from ProductPojo p where barcode=:barcode";
        TypedQuery<ProductPojo> query = entityManager.createQuery(jpql, ProductPojo.class);
        query.setParameter("barcode", barcode);
        return query.getResultList().stream().findFirst().orElse(null);
    }
}