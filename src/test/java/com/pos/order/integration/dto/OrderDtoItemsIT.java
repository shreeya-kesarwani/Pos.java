package com.pos.order.integration.dto;

import com.pos.dto.OrderDto;
import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDtoItemsIT extends AbstractIntegrationTest {

    @Autowired OrderDto orderDto;
    @Autowired TestFactory factory;

    @Test
    void shouldGetItems_happyFlow() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        var product = factory.createProduct("b1", "P1", client.getId(), 100.0, null);
        factory.createInventory(product.getId(), 50);
        flushAndClear();

        OrderItemForm item = new OrderItemForm();
        item.setBarcode("b1");
        item.setQuantity(1);
        item.setSellingPrice(5.0);

        OrderForm form = new OrderForm();
        form.setItems(List.of(item));

        Integer orderId = orderDto.create(form);
        flushAndClear();

        var items = orderDto.getItems(orderId);
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("b1", items.get(0).getBarcode());
    }

    @Test
    void shouldReturnItemsWithProductDetails() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        var product = factory.createProduct("b1", "P1", client.getId(), 100.0, null);
        factory.createInventory(product.getId(), 10);

        OrderItemForm item = new OrderItemForm();
        item.setBarcode("b1");
        item.setQuantity(1);
        item.setSellingPrice(10.0);

        OrderForm form = new OrderForm();
        form.setItems(List.of(item));

        Integer orderId = orderDto.create(form);
        flushAndClear();

        var out = orderDto.getItems(orderId);
        assertEquals(1, out.size());
        assertEquals("b1", out.getFirst().getBarcode());
        assertEquals("P1", out.getFirst().getProductName());
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
        // Create an order + insert an orderItem with non-existent productId
        var order = factory.createOrder(com.pos.model.constants.OrderStatus.CREATED, null);
        factory.createOrderItems(order.getId(),
                List.of(com.pos.setup.TestEntities.orderItem(order.getId(), 999999, 1, 10.0))
        );
        flushAndClear();

        assertThrows(ApiException.class, () -> orderDto.getItems(order.getId()));
    }
}