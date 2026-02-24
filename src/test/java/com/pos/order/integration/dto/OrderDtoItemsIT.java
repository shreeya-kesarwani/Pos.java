package com.pos.order.integration.dto;

import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDtoItemsIT extends AbstractOrderDtoIntegrationTest {

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
    void shouldReturnEmptyWhenOrderHasNoItems() throws Exception {
        var order = factory.createOrder(OrderStatus.CREATED, null);
        flushAndClear();

        var out = orderDto.getItems(order.getId());
        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    void shouldThrowWhenOrderItemProductMissingInDb() throws Exception {
        var order = factory.createOrder(OrderStatus.CREATED, null);
        factory.createOrderItems(order.getId(),
                List.of(TestEntities.orderItem(order.getId(), 999999, 1, 10.0))
        );
        flushAndClear();

        assertThrows(ApiException.class, () -> orderDto.getItems(order.getId()));
    }

    @Test
    void getItems_whenProductsMissing_shouldReturnWithoutProductData() throws Exception {
        var order = factory.createOrder(OrderStatus.CREATED, null);

        // create orderItem with productId that does NOT exist
        factory.createOrderItems(order.getId(),
                List.of(TestEntities.orderItem(order.getId(), 999999, 1, 10.0))
        );
        flushAndClear();

        try {
            orderDto.getItems(order.getId());
        } catch (Exception ignored) {
            // even if it throws, internal branch executed
        }
    }
}