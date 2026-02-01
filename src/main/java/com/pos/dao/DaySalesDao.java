package com.pos.dao;

import com.pos.pojo.DaySales;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class DaySalesDao extends BaseDao {

    public void insertOrUpdate(DaySales pojo) {
        em().merge(pojo);
    }

    // ✅ DaySales.date is LocalDate, so query using LocalDate
    public List<DaySales> selectBetweenDates(ZonedDateTime startUtcInclusive, ZonedDateTime endUtcExclusive) {
        String jpql = """
        SELECT d
        FROM DaySales d
        WHERE d.dayStartUtc >= :start
          AND d.dayStartUtc < :end
        ORDER BY d.dayStartUtc
    """;

        return em().createQuery(jpql, DaySales.class)
                .setParameter("start", startUtcInclusive)
                .setParameter("end", endUtcExclusive)
                .getResultList();
    }


    // ✅ This is correct to use ZonedDateTime because it filters Order.updatedAt (timestamp)
    public Object[] getAggregatesForDate(ZonedDateTime dayStartUtc) {

        ZonedDateTime nextDay = dayStartUtc.plusDays(1);

        String jpql = """
            SELECT COUNT(o), SUM(oi.quantity), SUM(oi.quantity * oi.sellingPrice)
            FROM Order o
            JOIN OrderItem oi ON oi.orderId = o.id
            WHERE o.status = 'INVOICED'
              AND o.updatedAt >= :start
              AND o.updatedAt < :end
        """;

        return em().createQuery(jpql, Object[].class)
                .setParameter("start", dayStartUtc)
                .setParameter("end", nextDay)
                .getSingleResult();
    }
}
