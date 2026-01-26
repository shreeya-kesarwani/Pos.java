package com.pos.dao;

import com.pos.pojo.Invoice;
import org.springframework.stereotype.Repository;

@Repository
public class InvoiceDao extends BaseDao {

    public Invoice selectByOrderId(Integer orderId) {
        String jpql = "SELECT i FROM Invoice i WHERE i.orderId = :orderId";
        return em().createQuery(jpql, Invoice.class)
                .setParameter("orderId", orderId)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }
}
