package com.pos.order.integration.dto;

import com.pos.dto.OrderDto;
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
}