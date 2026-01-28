package com.pos.dao;

import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class SalesReportDao extends BaseDao {

    @SuppressWarnings("unchecked")
    public List<Object[]> getSalesReportRows(
            LocalDate startDate,
            LocalDate endDate,
            Integer clientId
    ) {
        String sql =
                "SELECT " +
                        "p.barcode AS barcode, " +
                        "p.name AS productName, " +
                        "COALESCE(SUM(oi.quantity), 0) AS quantity, " +
                        "COALESCE(SUM(oi.quantity * oi.selling_price), 0) AS revenue " +
                        "FROM orders o " +
                        "JOIN order_item oi ON oi.order_id = o.id " +
                        "JOIN product p ON p.id = oi.product_id " +
                        "WHERE o.status = 'INVOICED' " +
                        "AND DATE(o.updated_at) BETWEEN :startDate AND :endDate " +
                        "AND (:clientId IS NULL OR p.client_id = :clientId) " +
                        "GROUP BY p.barcode, p.name " +
                        "ORDER BY p.name";

        Query q = em().createNativeQuery(sql);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        q.setParameter("clientId", clientId);

        return (List<Object[]>) q.getResultList();
    }
}
