package com.pos.dao;

import com.pos.pojo.Inventory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InventoryDao extends BaseDao {

    public void update(Inventory p) {
        em().merge(p);
    }

    public Inventory selectByProductId(Integer productId) {
        String jpql = "SELECT i FROM Inventory i WHERE i.productId = :productId";
        return em().createQuery(jpql, Inventory.class)
                .setParameter("productId", productId)
                .getResultList().stream().findFirst().orElse(null);
    }

    public List<Inventory> search(String barcode, String productName, String clientName, int page, int size) {
        String jpql = "SELECT i FROM Inventory i " +
                "JOIN Product p ON i.productId = p.id " +
                "JOIN Client c ON p.clientId = c.id " +
                "WHERE (:barcode IS NULL OR p.barcode = :barcode) " +
                "AND (:pName IS NULL OR p.name LIKE :pName) " +
                "AND (:cName IS NULL OR c.name = :cName)";

        return em().createQuery(jpql, Inventory.class)
                .setParameter("barcode", (barcode == null || barcode.isEmpty()) ? null : barcode)
                .setParameter("pName", (productName == null || productName.isEmpty()) ? null : "%" + productName + "%")
                .setParameter("cName", (clientName == null || clientName.isEmpty()) ? null : clientName)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long getCount(String barcode, String productName, String clientName) {
        String jpql = "SELECT COUNT(i) FROM Inventory i " +
                "JOIN Product p ON i.productId = p.id " +
                "JOIN Client c ON p.clientId = c.id " +
                "WHERE (:barcode IS NULL OR p.barcode = :barcode) " +
                "AND (:pName IS NULL OR p.name LIKE :pName) " +
                "AND (:cName IS NULL OR c.name = :cName)";

        return em().createQuery(jpql, Long.class)
                .setParameter("barcode", (barcode == null || barcode.isEmpty()) ? null : barcode)
                .setParameter("pName", (productName == null || productName.isEmpty()) ? null : "%" + productName + "%")
                .setParameter("cName", (clientName == null || clientName.isEmpty()) ? null : clientName)
                .getSingleResult();
    }

}