package com.pos.dao;

import com.pos.pojo.Inventory;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Repository
public class InventoryDao extends BaseDao {

    private static final String SELECT_BY_PRODUCT_ID = "SELECT i FROM Inventory i WHERE i.productId = :productId";
    private static final String SELECT_ALL = "SELECT i FROM Inventory i ORDER BY i.id";
    private static final String COUNT_ALL = "SELECT COUNT(i) FROM Inventory i";
    private static final String SELECT_BY_PRODUCT_IDS = "SELECT i FROM Inventory i WHERE i.productId IN :productIds ORDER BY i.id";
    private static final String COUNT_BY_PRODUCT_IDS = "SELECT COUNT(i) FROM Inventory i WHERE i.productId IN :productIds";

    public Inventory selectByProductId(Integer productId) {
        List<Inventory> list = createQuery(SELECT_BY_PRODUCT_ID, Inventory.class)
                .setParameter("productId", productId)
                .setMaxResults(1)
                .getResultList();

        return list.isEmpty() ? null : list.get(0);
    }

    public List<Inventory> findByProductIds(List<Integer> productIds, int page, int size) {
        if (productIds == null || productIds.isEmpty()) {
            return createQuery(SELECT_ALL, Inventory.class)
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .getResultList();
        }

        return createQuery(SELECT_BY_PRODUCT_IDS, Inventory.class)
                .setParameter("productIds", productIds)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long getCountByProductIds(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return createQuery(COUNT_ALL, Long.class).getSingleResult();
        }

        return createQuery(COUNT_BY_PRODUCT_IDS, Long.class)
                .setParameter("productIds", productIds)
                .getSingleResult();
    }

    public List<Inventory> selectByProductIds(List<Integer> productIds) {
        if (CollectionUtils.isEmpty(productIds)) return List.of();

        return createQuery(SELECT_BY_PRODUCT_IDS, Inventory.class)
                .setParameter("productIds", productIds)
                .getResultList();
    }

    public Inventory selectById(Integer id) {
        return select(id, Inventory.class);
    }
}
