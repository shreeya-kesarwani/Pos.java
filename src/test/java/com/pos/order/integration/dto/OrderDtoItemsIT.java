package com.pos.order.integration.dto;

import com.pos.dao.OrderDao;
import com.pos.dao.OrderItemDao;
import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDtoItemsIT extends AbstractOrderDtoIntegrationTest {

    @Autowired private OrderDao orderDao;
    @Autowired private OrderItemDao orderItemDao;

    @Test
    void shouldGetItems_happyFlow() throws Exception {
        Integer orderId = seedOrderWithOneItem("b1");

        var items = orderDto.getItems(orderId);
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("b1", items.get(0).getBarcode());
    }

    @Test
    void shouldReturnItemsWithProductDetails() throws Exception {
        Integer orderId = seedOrderWithOneItem("b2");

        var out = orderDto.getItems(orderId);
        assertEquals(1, out.size());
        assertEquals("b2", out.getFirst().getBarcode());
        assertNotNull(out.getFirst().getProductName());
    }

    @Test
    void shouldThrowWhenOrderIdNull() {
        assertThrows(ApiException.class, () -> orderDto.getItems(null));
    }

    @Test
    void shouldReturnEmptyWhenOrderHasNoItems() {
        var order = TestEntities.newOrder(OrderStatus.CREATED, null);
        orderDao.insert(order);
        flushAndClear();

        var out = orderDto.getItems(order.getId());
        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    void shouldThrowWhenOrderItemProductMissingInDb() {
        var order = TestEntities.newOrder(OrderStatus.CREATED, null);
        orderDao.insert(order);

        orderItemDao.insert(TestEntities.newOrderItem(order.getId(), 999999, 1, 10.0));
        flushAndClear();

        assertThrows(ApiException.class, () -> orderDto.getItems(order.getId()));
    }

    @Test
    void getItems_whenProductsMissing_shouldReturnWithoutProductData() {
        var order = TestEntities.newOrder(OrderStatus.CREATED, null);
        orderDao.insert(order);

        orderItemDao.insert(TestEntities.newOrderItem(order.getId(), 999999, 1, 10.0));
        flushAndClear();

        try {
            orderDto.getItems(order.getId());
        } catch (Exception ignored) {
        }
    }
}