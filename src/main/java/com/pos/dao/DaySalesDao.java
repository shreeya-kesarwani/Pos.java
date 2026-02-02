package com.pos.dao;

import com.pos.pojo.DaySales;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class DaySalesDao extends BaseDao {

    private static final String SELECT_BETWEEN_DATES_JPQL = """
        SELECT d
        FROM DaySales d
        WHERE d.date >= :start
          AND d.date < :end
        ORDER BY d.date
    """;

    private static final String GET_AGGREGATES_FOR_DATE_JPQL = """
        SELECT COUNT(o), SUM(oi.quantity), SUM(oi.quantity * oi.sellingPrice)
        FROM Order o
        JOIN OrderItem oi ON oi.orderId = o.id
        WHERE o.status = 'INVOICED'
          AND o.updatedAt >= :start
          AND o.updatedAt < :end
    """;

    public void insertOrUpdate(DaySales pojo) {
        em().merge(pojo);
    }

    public List<DaySales> selectBetweenDates(ZonedDateTime startUtcInclusive, ZonedDateTime endUtcExclusive) {
        return em().createQuery(SELECT_BETWEEN_DATES_JPQL, DaySales.class)
                .setParameter("start", startUtcInclusive)
                .setParameter("end", endUtcExclusive)
                .getResultList();
    }

    public Object[] getAggregatesForDate(ZonedDateTime dayStartUtc) {
        ZonedDateTime nextDay = dayStartUtc.plusDays(1);

        return em().createQuery(GET_AGGREGATES_FOR_DATE_JPQL, Object[].class)
                .setParameter("start", dayStartUtc)
                .setParameter("end", nextDay)
                .getSingleResult();
    }
}
