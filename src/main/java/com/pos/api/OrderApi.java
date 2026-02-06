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

import static com.pos.model.constants.ErrorMessages.*;

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
        Order order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ApiException(ORDER_NOT_FOUND.value() + ": " + orderId);
        }
        return order;
    }

    public void updateStatus(Integer orderId, OrderStatus newStatus) throws ApiException {
        if (newStatus == null) {
            throw new ApiException(STATUS_REQUIRED.value());
        }

        Order order = getCheck(orderId);

        if (order.getStatus() == OrderStatus.INVOICED) {
            throw new ApiException(CANNOT_CHANGE_INVOICED_ORDER.value() + ": " + orderId);
        }

        order.setStatus(newStatus);
    }

    public void attachInvoice(Integer orderId, String path) throws ApiException {
        if (path == null || path.isBlank()) {
            throw new ApiException(INVOICE_PATH_REQUIRED.value());
        }

        Order order = getCheck(orderId);

        if (order.getStatus() == OrderStatus.INVOICED) {
            throw new ApiException(ORDER_ALREADY_INVOICED.value() + ": " + orderId);
        }

        order.setInvoicePath(path);
        order.setStatus(OrderStatus.INVOICED);
    }

    @Transactional(readOnly = true)
    public List<Order> search(
            Integer id,
            ZonedDateTime start,
            ZonedDateTime end,
            OrderStatus status,
            int page,
            int size
    ) {
        return orderDao.search(id, start, end, status, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(
            Integer id,
            ZonedDateTime start,
            ZonedDateTime end,
            OrderStatus status
    ) {
        return orderDao.getCount(id, start, end, status);
    }
}
