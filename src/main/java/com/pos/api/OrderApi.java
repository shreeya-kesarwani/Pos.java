package com.pos.api;

import com.pos.dao.OrderDao;
import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.pojo.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Component
@Transactional(rollbackFor = ApiException.class)
public class OrderApi {

    @Autowired
    private OrderDao orderDao;

    public Order create() {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        orderDao.insert(order);
        return order;
    }

    @Transactional(readOnly = true)
    public Order getCheck(Integer orderId) throws ApiException {
        Order order = orderDao.select(orderId, Order.class);
        if (order == null) throw new ApiException("Order not found: " + orderId);
        return order;
    }

    public void updateStatus(Integer orderId, OrderStatus newStatus) throws ApiException {
        if (newStatus == null) {
            throw new ApiException("Status is required");
        }
        Order order = getCheck(orderId);
        OrderStatus current = order.getStatus();

        if (current == OrderStatus.INVOICED) {
            throw new ApiException("Cannot change status of an INVOICED order");
        }
        if (newStatus == OrderStatus.INVOICED) {
            throw new ApiException("Use attachInvoice() to mark an order INVOICED");
        }
        order.setStatus(newStatus);
    }

    public void attachInvoice(Integer orderId, String path) throws ApiException {
        if (path == null || path.isBlank()) {
            throw new ApiException("Invoice path is required");
        }
        Order order = getCheck(orderId);
        if (order.getStatus() == OrderStatus.INVOICED) {
            throw new ApiException("Order already invoiced");
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ApiException("Invoice can only be generated for CREATED orders");
        }
        order.setInvoicePath(path);
        order.setStatus(OrderStatus.INVOICED);
    }

    @Transactional(readOnly = true)
    public List<Order> search(Integer id, ZonedDateTime start, ZonedDateTime end, OrderStatus status, int page, int size) {
        return orderDao.search(id, start, end, status, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(Integer id, ZonedDateTime start, ZonedDateTime end, OrderStatus status) {
        return orderDao.getCount(id, start, end, status);
    }
}
