package com.pos.dao;

import com.pos.pojo.Product;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductDao extends BaseDao {

    public List<Product> search(String name, String barcode, String clientName, int page, int size) {
        String jpql = "SELECT p FROM Product p JOIN Client c ON p.clientId = c.id " +
                "WHERE (:name IS NULL OR p.name LIKE :name) " +
                "AND (:barcode IS NULL OR p.barcode = :barcode) " +
                "AND (:cName IS NULL OR c.name = :cName)";

        return em().createQuery(jpql, Product.class)
                .setParameter("name", (name == null || name.isEmpty()) ? null : "%" + name + "%")
                .setParameter("barcode", (barcode == null || barcode.isEmpty()) ? null : barcode)
                .setParameter("cName", (clientName == null || clientName.isEmpty()) ? null : clientName)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long getCount(String name, String barcode, String clientName) {
        String jpql = "SELECT COUNT(p) FROM Product p JOIN Client c ON p.clientId = c.id " +
                "WHERE (:name IS NULL OR p.name LIKE :name) " +
                "AND (:barcode IS NULL OR p.barcode = :barcode) " +
                "AND (:cName IS NULL OR c.name = :cName)";

        return em().createQuery(jpql, Long.class)
                .setParameter("name", (name == null || name.isEmpty()) ? null : "%" + name + "%")
                .setParameter("barcode", (barcode == null || barcode.isEmpty()) ? null : barcode)
                .setParameter("cName", (clientName == null || clientName.isEmpty()) ? null : clientName)
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