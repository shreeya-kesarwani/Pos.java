package com.pos.dao;

import com.pos.model.constants.OrderStatus;
import com.pos.pojo.Order;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class OrderDao extends BaseDao {

    private static final String SEARCH_JPQL =
            "SELECT o FROM Order o WHERE " +
                    "(:id IS NULL OR o.id = :id) " +
                    "AND (:status IS NULL OR o.status = :status) " +
                    "AND (:start IS NULL OR :end IS NULL OR o.createdAt BETWEEN :start AND :end) " +
                    "ORDER BY o.createdAt DESC";

    private static final String COUNT_JPQL =
            "SELECT COUNT(o) FROM Order o WHERE " +
                    "(:id IS NULL OR o.id = :id) " +
                    "AND (:status IS NULL OR o.status = :status) " +
                    "AND (:start IS NULL OR :end IS NULL OR o.createdAt BETWEEN :start AND :end)";

    public List<Order> search(Integer id, ZonedDateTime start, ZonedDateTime end, OrderStatus status, int page, int size) {

        return em().createQuery(SEARCH_JPQL, Order.class)
                .setParameter("id", id)
                .setParameter("status", status)
                .setParameter("start", start)
                .setParameter("end", end)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    public Long getCount(Integer id, ZonedDateTime start, ZonedDateTime end, OrderStatus status) {

        return em().createQuery(COUNT_JPQL, Long.class)
                .setParameter("id", id)
                .setParameter("status", status)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }
}
