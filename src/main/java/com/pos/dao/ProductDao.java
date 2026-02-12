package com.pos.dao;

import com.pos.pojo.Product;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Repository
public class ProductDao extends BaseDao {

    private static final String BASE_QUERY = """
        FROM Product p
        JOIN Client c ON p.clientId = c.id
        WHERE (:name IS NULL OR p.name LIKE :name)
          AND (:barcode IS NULL OR p.barcode = :barcode)
          AND (:cName IS NULL OR c.name = :cName)
    """;

    private static final String SEARCH_QUERY = "SELECT p " + BASE_QUERY + " ORDER BY p.id";
    private static final String COUNT_QUERY  = "SELECT COUNT(p) " + BASE_QUERY;

    private static final String SELECT_BY_BARCODES = "SELECT p FROM Product p WHERE p.barcode IN :barcodes";
    private static final String SELECT_BY_IDS = "SELECT p FROM Product p WHERE p.id IN :ids";

    private static final String FETCH_IDS = """
        SELECT p.id
        FROM Product p
        WHERE (:barcode IS NULL OR p.barcode = :barcode)
          AND (:name IS NULL OR p.name LIKE :name)
        ORDER BY p.id
    """;

    public List<Product> search(String name, String barcode, String clientName, int page, int size) {
        return createQuery(SEARCH_QUERY, Product.class)
                .setParameter("name", like(name))
                .setParameter("barcode", barcode)
                .setParameter("cName", clientName)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long getCount(String name, String barcode, String clientName) {
        return createQuery(COUNT_QUERY, Long.class)
                .setParameter("name", like(name))
                .setParameter("barcode", barcode)
                .setParameter("cName", clientName)
                .getSingleResult();
    }

    public List<Integer> findProductIdsByBarcodeOrName(String barcode, String productName) {
        return createQuery(FETCH_IDS, Integer.class)
                .setParameter("barcode", barcode)
                .setParameter("name", like(productName))
                .getResultList();
    }

    public List<Product> selectByBarcodes(List<String> barcodes) {
        if (CollectionUtils.isEmpty(barcodes)) return List.of();

        return createQuery(SELECT_BY_BARCODES, Product.class)
                .setParameter("barcodes", barcodes)
                .getResultList();
    }

    public List<Product> selectByIds(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) return List.of();

        return createQuery(SELECT_BY_IDS, Product.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    public Product selectById(Integer id) {
        return select(id, Product.class);
    }
}
