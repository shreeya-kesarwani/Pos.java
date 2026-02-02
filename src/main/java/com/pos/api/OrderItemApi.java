package com.pos.api;

import com.pos.dao.OrderItemDao;
import com.pos.exception.ApiException;
import com.pos.pojo.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(rollbackFor = ApiException.class)
public class OrderItemApi {

    @Autowired
    private OrderItemDao orderItemDao;

    public void add(OrderItem item) {
        orderItemDao.insert(item);
    }

    /**
     * ✅ API-level entity creation helper
     * This is OK in API layer (Flow should not build entities).
     */
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

    /**
     * ✅ Optional helper if you want "getCheck" style.
     * Useful for places where empty order items should be treated as error.
     */
    @Transactional(readOnly = true)
    public List<OrderItem> getCheckByOrderId(Integer orderId) throws ApiException {
        List<OrderItem> items = orderItemDao.selectByOrderId(orderId);
        if (items == null || items.isEmpty()) {
            throw new ApiException("No order items found for order: " + orderId);
        }
        return items;
    }
}
