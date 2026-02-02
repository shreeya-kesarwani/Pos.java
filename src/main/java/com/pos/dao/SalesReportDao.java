package com.pos.dao;

import com.pos.pojo.SalesReport;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class SalesReportDao extends BaseDao {

    private static final String SALES_REPORT_SQL =
            "SELECT " +
                    "p.barcode AS barcode, " +
                    "p.name AS productName, " +
                    "COALESCE(SUM(oi.quantity), 0) AS quantity, " +
                    "COALESCE(SUM(oi.quantity * oi.selling_price), 0) AS revenue " +
                    "FROM pos_order o " +
                    "JOIN pos_order_item oi ON oi.order_id = o.id " +
                    "JOIN pos_product p ON p.id = oi.product_id " +
                    "WHERE o.status = 'INVOICED' " +
                    "AND DATE(o.updated_at) BETWEEN :startDate AND :endDate " +
                    "AND (:clientId IS NULL OR p.client_id = :clientId) " +
                    "GROUP BY p.barcode, p.name " +
                    "ORDER BY p.name";

    @SuppressWarnings("unchecked")
    public List<SalesReport> getSalesReportRows(LocalDate startDate, LocalDate endDate, Integer clientId) {

        Query q = em().createNativeQuery(SALES_REPORT_SQL);
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        q.setParameter("clientId", clientId);

        List<Object[]> rows = (List<Object[]>) q.getResultList();

        return rows.stream().map(r -> {
            SalesReport row = new SalesReport();
            row.setBarcode((String) r[0]);
            row.setProductName((String) r[1]);

            // ✅ FIX: SUM(...) typically returns Long/BigInteger -> cast via Number -> intValue()
            Number qtyNum = (Number) r[2];
            row.setQuantity(qtyNum == null ? 0 : qtyNum.intValue());

            // ✅ FIX: revenue can come as BigDecimal/Double/Long -> normalize via Number
            Number revNum = (Number) r[3];
            row.setRevenue(revNum == null ? 0.0 : revNum.doubleValue());

            return row;
        }).collect(Collectors.toList());
    }
}
