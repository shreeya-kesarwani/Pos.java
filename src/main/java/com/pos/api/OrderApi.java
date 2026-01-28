package com.pos.api;

import com.pos.dao.OrderDao;
import com.pos.exception.ApiException;
import com.pos.model.data.OrderStatus;
import com.pos.pojo.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class OrderApi {

    @Autowired
    private OrderDao orderDao;

    public Order create() {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        orderDao.insert(order);
        return order;
    }

    public Order getCheck(Integer orderId) throws ApiException {
        Order order = orderDao.select(orderId);
        if (order == null) {
            throw new ApiException("Order not found: " + orderId);
        }
        return order;
    }

    public void updateStatus(Integer orderId, OrderStatus status) throws ApiException {
        Order order = getCheck(orderId);
        order.setStatus(status);
        orderDao.update(order);
    }

    public List<Order> search(
            Integer id,
            ZonedDateTime start,
            ZonedDateTime end,
            OrderStatus status,
            int page,
            int size) {
        return orderDao.search(id, start, end, status, page, size);
    }

    public Long getCount(
            Integer id,
            ZonedDateTime start,
            ZonedDateTime end,
            OrderStatus status) {
        return orderDao.getCount(id, start, end, status);
    }

    public void attachInvoice(Integer orderId, String path)
            throws ApiException {

        Order order = getCheck(orderId);

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ApiException(
                    "Invoice can only be generated for CREATED orders"
            );
        }
        order.setInvoicePath(path);
        order.setStatus(OrderStatus.INVOICED);
        orderDao.update(order);
    }
}
