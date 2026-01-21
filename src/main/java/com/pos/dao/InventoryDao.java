package com.pos.dao;

import com.pos.pojo.InventoryPojo;
import org.springframework.stereotype.Repository;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Repository
public class InventoryDao extends BaseDao {

    public InventoryPojo selectByProductId(Integer productId) {
        String jpql = "select i from InventoryPojo i where i.productId = :productId";
        TypedQuery<InventoryPojo> query = entityManager.createQuery(jpql, InventoryPojo.class);
        query.setParameter("productId", productId);
        return getSingle(query);
    }

    public List<InventoryPojo> search(String barcode, String productName, String clientName) {
        StringBuilder jpql = new StringBuilder("select i from InventoryPojo i, ProductPojo p, ClientPojo c ");
        jpql.append("where i.productId = p.id and p.clientId = c.id ");
        jpql.append("and (:barcode is null or p.barcode like :barcode) ");
        jpql.append("and (:pName is null or p.name like :pName) ");
        jpql.append("and (:cName is null or c.name like :cName)");

        TypedQuery<InventoryPojo> query = entityManager.createQuery(jpql.toString(), InventoryPojo.class);
        query.setParameter("barcode", barcode == null ? null : "%" + barcode + "%");
        query.setParameter("pName", productName == null ? null : "%" + productName + "%");
        query.setParameter("cName", clientName == null ? null : "%" + clientName + "%");

        return query.getResultList();
    }
}