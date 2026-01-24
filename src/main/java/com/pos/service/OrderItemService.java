//package com.pos.service;
//
//import com.pos.dao.OrderItemDao;
//import com.pos.model.form.OrderItemForm;
//import com.pos.pojo.OrderItemPojo;
//import com.pos.pojo.ProductPojo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class OrderItemService {
//
//    @Autowired
//    private OrderItemDao dao;
//
//    public void create(Integer orderId,
//                       List<OrderItemForm> items,
//                       Map<String, ProductPojo> productMap) {
//
//        for (OrderItemForm f : items) {
//            OrderItemPojo p = new OrderItemPojo();
//            p.setOrderId(orderId);
//            p.setProductId(productMap.get(f.getBarcode()).getId());
//            p.setBarcode(f.getBarcode());
//            p.setQuantity(f.getQuantity());
//            p.setSellingPrice(f.getSellingPrice());
//            dao.insert(p);
//        }
//    }
//
//    public List<OrderItemPojo> getByOrderId(Integer orderId) {
//        return dao.selectByOrderId(orderId);
//    }
//}
