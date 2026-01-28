package com.pos.dao;

import com.pos.pojo.DaySales;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Query;
import java.time.LocalDate;
import java.util.List;

@Repository
public class DaySalesDao extends BaseDao {

    public void insertOrUpdate(DaySales pojo) {
        em().merge(pojo);
    }

    public List<DaySales> selectBetweenDates(LocalDate start, LocalDate end) {

        String jpql =
                "SELECT d FROM DaySales d " +
                        "WHERE d.date BETWEEN :start AND :end " +
                        "ORDER BY d.date";

        return em().createQuery(jpql, DaySales.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

//    public Object[] getAggregatesForDate(LocalDate date) {
//
//        String sql =
//                "SELECT " +
//                        "COUNT(DISTINCT i.id), " +
//                        "COALESCE(SUM(ii.quantity), 0), " +
//                        "COALESCE(SUM(ii.quantity * ii.selling_price), 0) " +
//                        "FROM invoice i " +
//                        "LEFT JOIN invoice_item ii ON ii.invoice_id = i.id " +
//                        "WHERE i.invoice_date = :date";
//
//        Query query = em().createNativeQuery(sql);
//        query.setParameter("date", date);
//
//        return (Object[]) query.getSingleResult();
//    }

    public Object[] getAggregatesForDate(LocalDate date) {

        String sql = """
        SELECT
          COUNT(DISTINCT o.id) AS invoiced_orders_count,
          COALESCE(SUM(oi.quantity), 0) AS invoiced_items_count,
          COALESCE(SUM(oi.quantity * oi.selling_price), 0) AS total_revenue
        FROM orders o
        LEFT JOIN order_item oi ON oi.order_id = o.id
        WHERE o.status = 'INVOICED'
          AND DATE(o.updated_at) = ?
        """;

        Query q = em().createNativeQuery(sql);
        q.setParameter(1, java.sql.Date.valueOf(date));

        return (Object[]) q.getSingleResult();
    }
}
