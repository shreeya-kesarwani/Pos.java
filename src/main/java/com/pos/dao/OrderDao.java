//package com.pos.dao;
//
//import com.pos.model.data.OrderStatus;
//import com.pos.pojo.OrderPojo;
//import jakarta.persistence.criteria.*;
//import org.springframework.stereotype.Repository;
//
//import java.time.ZonedDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Repository
//public class OrderDao extends BaseDao {
//
//    public void insert(OrderPojo p) {
//        em().persist(p);
//    }
//
//    public OrderPojo select(Integer id) {
//        return em().find(OrderPojo.class, id);
//    }
//
//    public void update(OrderPojo p) {
//        em().merge(p);
//    }
//
//    public List<OrderPojo> search(Integer id,
//                                  ZonedDateTime start,
//                                  ZonedDateTime end,
//                                  OrderStatus status) {
//
//        CriteriaBuilder cb = em().getCriteriaBuilder();
//        CriteriaQuery<OrderPojo> cq = cb.createQuery(OrderPojo.class);
//        Root<OrderPojo> root = cq.from(OrderPojo.class);
//
//        List<Predicate> predicates = new ArrayList<>();
//
//        if (id != null) predicates.add(cb.equal(root.get("id"), id));
//        if (status != null) predicates.add(cb.equal(root.get("status"), status));
//        if (start != null && end != null)
//            predicates.add(cb.between(root.get("createdAt"), start, end));
//
//        cq.where(predicates.toArray(new Predicate[0]));
//        return em().createQuery(cq).getResultList();
//    }
//}
