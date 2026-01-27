package com.pos.dao;

import com.pos.model.data.OrderStatus;
import com.pos.pojo.Order;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class OrderDao extends BaseDao {

    /**
     * Insert new order
     */
    public void insert(Order order) {
        em().persist(order);
    }

    /**
     * Fetch order by ID
     */
    public Order select(Integer id) {
        return em().find(Order.class, id);
    }

    /**
     * Update order (status change)
     */
    public void update(Order order) {
        em().merge(order);
    }

    /**
     * Search orders by:
     * - orderId (optional)
     * - status (optional)
     * - createdAt range (optional)
     */
//    public List<Order> search(Integer id,
//                              ZonedDateTime start,
//                              ZonedDateTime end,
//                              OrderStatus status) {
//
//        String jpql =
//                "SELECT o FROM Order o WHERE " +
//                        "(:id IS NULL OR o.id = :id) " +
//                        "AND (:status IS NULL OR o.status = :status) " +
//                        "AND (:start IS NULL OR :end IS NULL OR o.createdAt BETWEEN :start AND :end) " +
//                        "ORDER BY o.createdAt DESC";
//
//        return em().createQuery(jpql, Order.class)
//                .setParameter("id", id)
//                .setParameter("status", status)
//                .setParameter("start", start)
//                .setParameter("end", end)
//                .getResultList();
//    }

    public List<Order> search(Integer id,
                              ZonedDateTime start,
                              ZonedDateTime end,
                              OrderStatus status,
                              int page,
                              int size) {

        String jpql =
                "SELECT o FROM Order o WHERE " +
                        "(:id IS NULL OR o.id = :id) " +
                        "AND (:status IS NULL OR o.status = :status) " +
                        "AND (:start IS NULL OR :end IS NULL OR o.createdAt BETWEEN :start AND :end) " +
                        "ORDER BY o.createdAt DESC";

        return em().createQuery(jpql, Order.class)
                .setParameter("id", id)
                .setParameter("status", status)
                .setParameter("start", start)
                .setParameter("end", end)
                .setFirstResult(page * size)   // ✅ pagination
                .setMaxResults(size)           // ✅ pagination
                .getResultList();
    }

    public Long getCount(Integer id,
                         ZonedDateTime start,
                         ZonedDateTime end,
                         OrderStatus status) {

        String jpql =
                "SELECT COUNT(o) FROM Order o WHERE " +
                        "(:id IS NULL OR o.id = :id) " +
                        "AND (:status IS NULL OR o.status = :status) " +
                        "AND (:start IS NULL OR :end IS NULL OR o.createdAt BETWEEN :start AND :end)";

        return em().createQuery(jpql, Long.class)
                .setParameter("id", id)
                .setParameter("status", status)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

}
