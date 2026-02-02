package com.pos.dao;

import com.pos.pojo.Product;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class ProductDao extends BaseDao {

    private static final String BASE_QUERY =
            " FROM Product p, Client c " +
                    " WHERE p.clientId = c.id " +
                    " AND (:name IS NULL OR p.name LIKE :name) " +
                    " AND (:barcode IS NULL OR p.barcode = :barcode) " +
                    " AND (:cName IS NULL OR c.name = :cName)";

    public List<Product> search(String name, String barcode, String clientName, int page, int size) {

        String jpql = "SELECT p" + BASE_QUERY + " ORDER BY p.id";

        return em().createQuery(jpql, Product.class)
                .setParameter("name", name)
                .setParameter("barcode", barcode)
                .setParameter("cName", clientName)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long getCount(String name, String barcode, String clientName) {
        String jpql = "SELECT COUNT(p)" + BASE_QUERY;

        return em().createQuery(jpql, Long.class)
                .setParameter("name", name)
                .setParameter("barcode", barcode)
                .setParameter("cName", clientName)
                .getSingleResult();
    }

    public List<Product> selectByBarcodes(List<String> barcodes) {
        if (barcodes == null || barcodes.isEmpty()) return List.of();

        String jpql = "SELECT p FROM Product p WHERE p.barcode IN :barcodes";
        return em().createQuery(jpql, Product.class)
                .setParameter("barcodes", barcodes)
                .getResultList();
    }

    public List<Product> selectByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        String jpql = "SELECT p FROM Product p WHERE p.id IN :ids";
        return em().createQuery(jpql, Product.class)
                .setParameter("ids", ids)
                .getResultList();
    }
}
