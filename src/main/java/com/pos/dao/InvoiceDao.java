//package com.pos.dao;
//
//import com.pos.pojo.InvoicePojo;
//import org.springframework.stereotype.Repository;
//import jakarta.persistence.TypedQuery;
//
//@Repository
//public class InvoiceDao extends BaseDao {
//    public void insert(InvoicePojo p) {
//        em().persist(p);
//    }
//
//    public InvoicePojo selectByOrderId(Integer orderId) {
//        String hql = "select p from InvoicePojo p where orderId=:orderId";
//        TypedQuery<InvoicePojo> query = em().createQuery(hql, InvoicePojo.class);
//        query.setParameter("orderId", orderId);
//        return query.getResultList().stream().findFirst().orElse(null);
//    }
//}