//package com.pos.service;
//
//import com.pos.dao.OrderDao;
//import com.pos.exception.ApiException;
//import com.pos.model.data.OrderStatus;
//import com.pos.pojo.OrderPojo;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.ZonedDateTime;
//import java.util.List;
//
//@Service
//public class OrderService {
//
//    @Autowired
//    private OrderDao orderDao;
//
//    public OrderPojo create() {
//        OrderPojo p = new OrderPojo();
//        p.setStatus(OrderStatus.CREATED);
//        orderDao.insert(p);
//        return p;
//    }
//
//    public OrderPojo getCheck(Integer id) {
//        OrderPojo p = orderDao.select(id);
//        if (p == null) {
//            throw new ApiException("Order not found: " + id);
//        }
//        return p;
//    }
//
//    public void updateStatus(Integer id, OrderStatus status) {
//        OrderPojo p = getCheck(id);
//        p.setStatus(status);
//        orderDao.update(p);
//    }
//
//    public List<OrderPojo> search(Integer id,
//                                  ZonedDateTime start,
//                                  ZonedDateTime end,
//                                  OrderStatus status) {
//        return orderDao.search(id, start, end, status);
//    }
//}
