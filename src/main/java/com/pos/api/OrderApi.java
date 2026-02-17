package com.pos.api;

import com.pos.dao.OrderDao;
import com.pos.dao.OrderItemDao;
import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.List;

import static com.pos.model.constants.ErrorMessages.*;

@Component
@Transactional(rollbackFor = Exception.class)
public class OrderApi {

    @Autowired private OrderDao orderDao;
    @Autowired private OrderItemDao orderItemDao;

    public Integer create(List<OrderItem> items) throws ApiException {

        if (CollectionUtils.isEmpty(items)) {
            throw new ApiException(NO_ORDER_ITEMS_FOUND.value());
        }

        for (OrderItem item : items) {
            validateOrderItem(item.getProductId(), item.getQuantity(), item.getSellingPrice());
        }

        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        orderDao.insert(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderItemDao.insert(item);
        }
        return order.getId();
    }

    @Transactional(readOnly = true)
    public Order getCheck(Integer orderId) throws ApiException {
        Order order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ApiException(ORDER_NOT_FOUND.value() + ": " + orderId);
        }
        return order;
    }

    public void generateInvoice(Integer orderId, String path) throws ApiException {
        if (path == null || path.isBlank()) {
            throw new ApiException(INVOICE_PATH_REQUIRED.value());
        }
        Order order = getCheck(orderId);
        order.setInvoicePath(path);
        order.setStatus(OrderStatus.INVOICED);
    }

    @Transactional(readOnly = true)
    public List<Order> search(Integer id, ZonedDateTime start, ZonedDateTime end, OrderStatus status, int page, int size) {
        return orderDao.search(id, start, end, status, page, size);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getItemsByOrderId(Integer orderId) {
        getCheck(orderId);
        return orderItemDao.selectByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getCheckItemsByOrderId(Integer orderId) throws ApiException {
        List<OrderItem> items = orderItemDao.selectByOrderId(orderId);
        if (CollectionUtils.isEmpty(items)) {
            throw new ApiException(NO_ORDER_ITEMS_FOUND.value() + ": orderId=" + orderId);
        }
        return items;
    }

    @Transactional(readOnly = true)
    public Long getCount(Integer id, ZonedDateTime start, ZonedDateTime end, OrderStatus status) {
        return orderDao.getCount(id, start, end, status);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getItemsByOrderIds(List<Integer> orderIds) {
        return orderItemDao.selectByOrderIds(orderIds);
    }

    private void validateOrderItem(Integer productId, Integer quantity, Double sellingPrice) throws ApiException {

        if (productId == null) {
            throw new ApiException(PRODUCT_NOT_FOUND.value());
        }

        if (quantity == null || quantity <= 0) {
            throw new ApiException(QUANTITY_MUST_BE_POSITIVE.value());
        }

        if (sellingPrice == null || sellingPrice < 0) {
            throw new ApiException(SELLING_PRICE_CANNOT_BE_NEGATIVE.value());
        }
    }
}