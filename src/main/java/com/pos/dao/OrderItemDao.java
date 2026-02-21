package com.pos.dao;

import com.pos.pojo.OrderItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderItemDao extends BaseDao {

    private static final String SELECT_BY_ORDER_ID = "SELECT oi FROM OrderItem oi WHERE oi.orderId = :orderId ORDER BY oi.id";
    private static final String SELECT_BY_ORDER_IDS = "SELECT oi FROM OrderItem oi WHERE oi.orderId IN :orderIds ORDER BY oi.orderId, oi.id";

    public List<OrderItem> selectByOrderId(Integer orderId) {
        return createQuery(SELECT_BY_ORDER_ID, OrderItem.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public List<OrderItem> selectByOrderIds(List<Integer> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) return List.of();
        return createQuery(SELECT_BY_ORDER_IDS, OrderItem.class)
                .setParameter("orderIds", orderIds)
                .getResultList();
    }
}

