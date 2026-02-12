package com.pos.dao;

import com.pos.pojo.DaySales;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class DaySalesDao extends BaseDao {

    private static final String SELECT_IN_RANGE = """
        SELECT d
        FROM DaySales d
        WHERE d.date >= :start
          AND d.date < :end
        ORDER BY d.date
    """;

    private static final String SELECT_INVOICED_SALES_AGGREGATES_FOR_DAY = """
        SELECT COUNT(DISTINCT o.id),
               COALESCE(SUM(oi.quantity), 0),
               COALESCE(SUM(oi.quantity * oi.sellingPrice), 0)
        FROM Order o
        JOIN OrderItem oi ON oi.orderId = o.id
        WHERE o.status = 'INVOICED'
          AND o.updatedAt >= :start
          AND o.updatedAt < :end
    """;

    public List<DaySales> selectInRange(ZonedDateTime start, ZonedDateTime end) {
        return createQuery(SELECT_IN_RANGE, DaySales.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    public Object[] selectInvoicedSalesAggregatesForDay(ZonedDateTime start, ZonedDateTime end) {
        return createQuery(SELECT_INVOICED_SALES_AGGREGATES_FOR_DAY, Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }
}
