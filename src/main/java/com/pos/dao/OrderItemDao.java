package com.pos.dao;

import com.pos.pojo.OrderItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderItemDao extends BaseDao {

    private static final String SELECT_BY_ORDER_ID = "SELECT oi FROM OrderItem oi WHERE oi.orderId = :orderId";

    public List<OrderItem> selectByOrderId(Integer orderId) {
        return createQuery(SELECT_BY_ORDER_ID, OrderItem.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }
}
