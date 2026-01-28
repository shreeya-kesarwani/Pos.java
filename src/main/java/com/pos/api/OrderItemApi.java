package com.pos.api;

import com.pos.dao.OrderItemDao;
import com.pos.pojo.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderItemApi {

    @Autowired
    private OrderItemDao orderItemDao;

    public void add(OrderItem item) {
        orderItemDao.insert(item);
    }

    public void add(Integer orderId, Integer productId, Integer quantity, Double sellingPrice) {
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setSellingPrice(sellingPrice);
        orderItemDao.insert(item);
    }

    public List<OrderItem> getByOrderId(Integer orderId) {
        return orderItemDao.selectByOrderId(orderId);
    }
}

