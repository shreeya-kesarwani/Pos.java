package com.pos.dao;

import com.pos.pojo.Inventory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InventoryDao extends BaseDao {

    private static final String SELECT_BY_PRODUCT_ID_JPQL =
            "SELECT i FROM Inventory i WHERE i.productId = :productId";

    private static final String BASE_QUERY =
            " FROM Inventory i " +
                    " JOIN Product p ON i.productId = p.id " +
                    " JOIN Client c ON p.clientId = c.id " +
                    " WHERE (:barcode IS NULL OR p.barcode = :barcode) " +
                    " AND (:pName IS NULL OR p.name LIKE :pName) " +
                    " AND (:cName IS NULL OR c.name = :cName)";

    public Inventory selectByProductId(Integer productId) {
        return em().createQuery(SELECT_BY_PRODUCT_ID_JPQL, Inventory.class)
                .setParameter("productId", productId)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<Inventory> search(String barcode, String productName, String clientName, int page, int size) {
        String jpql = "SELECT i" + BASE_QUERY + " ORDER BY i.id";

        return em().createQuery(jpql, Inventory.class)
                .setParameter("barcode", barcode)
                .setParameter("pName", productName)
                .setParameter("cName", clientName)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long getCount(String barcode, String productName, String clientName) {
        String jpql = "SELECT COUNT(i)" + BASE_QUERY;

        return em().createQuery(jpql, Long.class)
                .setParameter("barcode", barcode)
                .setParameter("pName", productName)
                .setParameter("cName", clientName)
                .getSingleResult();
    }
}
