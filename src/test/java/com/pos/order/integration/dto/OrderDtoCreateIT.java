package com.pos.order.integration.dto;

import com.pos.dto.OrderDto;
import com.pos.exception.ApiException;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDtoCreateIT extends AbstractIntegrationTest {

    @Autowired OrderDto orderDto;
    @Autowired TestFactory factory;

    @Test
    void shouldCreateOrder_happyFlow() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        var product = factory.createProduct("b1", "P1", client.getId(), 100.0, null);
        factory.createInventory(product.getId(), 50);
        flushAndClear();

        OrderItemForm item = new OrderItemForm();
        item.setBarcode("  b1  ");
        item.setQuantity(2);
        item.setSellingPrice(10.0);

        OrderForm form = new OrderForm();
        form.setItems(List.of(item));

        Integer orderId = orderDto.create(form);
        assertNotNull(orderId);
    }

    @Test
    void shouldThrowWhenCreateBarcodeBlank() {
        OrderItemForm item = new OrderItemForm();
        item.setBarcode("   ");
        item.setQuantity(1);
        item.setSellingPrice(5.0);

        OrderForm form = new OrderForm();
        form.setItems(List.of(item));

        assertThrows(ApiException.class, () -> orderDto.create(form));
    }
}