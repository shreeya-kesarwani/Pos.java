//package com.pos.dao;
//
//import com.pos.pojo.OrderItemPojo;
//import jakarta.persistence.criteria.*;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public class OrderItemDao extends BaseDao {
//
//    public void insert(OrderItemPojo p) {
//        em().persist(p);
//    }
//
//    public List<OrderItemPojo> selectByOrderId(Integer orderId) {
//        CriteriaBuilder cb = em().getCriteriaBuilder();
//        CriteriaQuery<OrderItemPojo> cq = cb.createQuery(OrderItemPojo.class);
//        Root<OrderItemPojo> root = cq.from(OrderItemPojo.class);
//
//        cq.where(cb.equal(root.get("orderId"), orderId));
//        return em().createQuery(cq).getResultList();
//    }
//}
