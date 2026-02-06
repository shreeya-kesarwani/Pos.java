package com.pos.dao;

import com.pos.pojo.Inventory;
import com.pos.pojo.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InventoryDao extends BaseDao {

    private static final String SELECT_BY_PRODUCT_ID = "SELECT i FROM Inventory i WHERE i.productId = :productId";
    private static final String SEARCH_COUNT_BASE_QUERY =
            " FROM Inventory i " +
                    " JOIN Product p ON i.productId = p.id " +
                    " WHERE (:barcode IS NULL OR p.barcode = :barcode) " +
                    " AND (:pName IS NULL OR p.name LIKE :pName) ";

    private static final String SEARCH_QUERY = "SELECT i" + SEARCH_COUNT_BASE_QUERY + " ORDER BY i.id";
    private static final String COUNT_QUERY = "SELECT COUNT(i)" + SEARCH_COUNT_BASE_QUERY;

    public Inventory selectByProductId(Integer productId) {
        List<Inventory> list = createQuery(SELECT_BY_PRODUCT_ID, Inventory.class)
                .setParameter("productId", productId)
                .setMaxResults(1)
                .getResultList();

        return list.isEmpty() ? null : list.get(0);
    }

    public List<Inventory> search(String barcode, String productName, int page, int size) {
        return createQuery(SEARCH_QUERY, Inventory.class)
                .setParameter("barcode", barcode)
                .setParameter("pName", like(productName))
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long getCount(String barcode, String productName) {
        return createQuery(COUNT_QUERY, Long.class)
                .setParameter("barcode", barcode)
                .setParameter("pName", like(productName))
                .getSingleResult();
    }

    public Inventory selectById(Integer id) {
        return select(id, Inventory.class);
    }

    private String like(String value) {
        return value == null ? null : "%" + value + "%";
    }
}
