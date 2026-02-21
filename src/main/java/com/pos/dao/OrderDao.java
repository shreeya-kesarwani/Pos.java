package com.pos.dao;

import com.pos.model.constants.OrderStatus;
import com.pos.pojo.Order;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class OrderDao extends BaseDao {

    private static final String ORDER_FILTERS = """
        FROM Order o
        WHERE (:id IS NULL OR o.id = :id)
          AND (:status IS NULL OR o.status = :status)
          AND (:start IS NULL OR o.updatedAt >= :start)
          AND (:end IS NULL OR o.updatedAt < :end)
        """;

    private static final String ORDER_SEARCH = "SELECT o " + ORDER_FILTERS + " ORDER BY o.updatedAt DESC";

    private static final String ORDER_COUNT = "SELECT COUNT(o) " + ORDER_FILTERS;

    public List<Order> search(Integer id, ZonedDateTime start, ZonedDateTime end, OrderStatus status, int page, int size) {

        return createQuery(ORDER_SEARCH, Order.class)
                .setParameter("id", id)
                .setParameter("status", status)
                .setParameter("start", start)
                .setParameter("end", end)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long getCount(Integer id, ZonedDateTime start, ZonedDateTime end, OrderStatus status) {

        return createQuery(ORDER_COUNT, Long.class)
                .setParameter("id", id)
                .setParameter("status", status)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

    public Order selectById(Integer id) {
        return select(id, Order.class);
    }
}
