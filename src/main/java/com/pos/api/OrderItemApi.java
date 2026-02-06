package com.pos.api;

import com.pos.dao.OrderItemDao;
import com.pos.exception.ApiException;
import com.pos.pojo.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.pos.model.constants.ErrorMessages.*;

@Component
@Transactional(rollbackFor = ApiException.class)
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

    @Transactional(readOnly = true)
    public List<OrderItem> getByOrderId(Integer orderId) {
        return orderItemDao.selectByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getCheckByOrderId(Integer orderId) throws ApiException {
        List<OrderItem> items = orderItemDao.selectByOrderId(orderId);
        if (items == null || items.isEmpty()) {
            throw new ApiException(NO_ORDER_ITEMS_FOUND.value() + ": orderId=" + orderId);
        }
        return items;
    }
}
