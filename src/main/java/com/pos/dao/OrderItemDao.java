package com.pos.dao;

import com.pos.pojo.OrderItem;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderItemDao extends BaseDao {

    public void insert(OrderItem item) {
        em().persist(item);
    }

    public List<OrderItem> selectByOrderId(Integer orderId) {

        String jpql = "SELECT oi FROM OrderItem oi WHERE oi.orderId = :orderId";

        return em().createQuery(jpql, OrderItem.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }
}
