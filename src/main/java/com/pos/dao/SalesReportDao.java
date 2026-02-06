package com.pos.dao;

import com.pos.model.data.SalesReportData;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class SalesReportDao extends BaseDao {

    // todo ponder on if day_sales and sales_report can be merged
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

    public List<SalesReportData> getSalesReportRows(
            LocalDate startDate, LocalDate endDate, Integer clientId) {

        Query query = createNativeQuery(SALES_REPORT_SQL)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .setParameter("clientId", clientId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        return rows.stream().map(r -> {
            SalesReportData row = new SalesReportData();
            row.setBarcode((String) r[0]);
            row.setProductName((String) r[1]);

            Number qtyNum = (Number) r[2];
            row.setQuantity(qtyNum == null ? 0 : qtyNum.intValue());

            Number revNum = (Number) r[3];
            row.setRevenue(revNum == null ? 0.0 : revNum.doubleValue());

            return row;
        }).collect(Collectors.toList());
    }
}
