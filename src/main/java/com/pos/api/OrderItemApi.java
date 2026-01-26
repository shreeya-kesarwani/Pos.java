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

    public List<OrderItem> getByOrderId(Integer orderId) {
        return orderItemDao.selectByOrderId(orderId);
    }
}
