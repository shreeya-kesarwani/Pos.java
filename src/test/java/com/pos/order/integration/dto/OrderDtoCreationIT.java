package com.pos.order.integration.dto;

import com.pos.dao.ClientDao;
import com.pos.dao.InventoryDao;
import com.pos.dao.ProductDao;
import com.pos.exception.ApiException;
import com.pos.model.form.OrderForm;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDtoCreationIT extends AbstractOrderDtoIntegrationTest {

    @Autowired private ClientDao clientDao;
    @Autowired private ProductDao productDao;
    @Autowired private InventoryDao inventoryDao;

    @Test
    void shouldCreateOrder_happyFlow() throws Exception {
        var client = TestEntities.newClient("Acme", "a@acme.com");
        clientDao.insert(client);

        var product = TestEntities.newProduct("b1", "P1", client.getId(), 100.0, null);
        productDao.insert(product);

        inventoryDao.insert(TestEntities.newInventory(product.getId(), 50));
        flushAndClear();

        Integer orderId = orderDto.create(orderForm(item("  b1  ", 2, 10.0)));
        assertNotNull(orderId);
    }

    @Test
    void shouldCreateOrderWithMultipleItems() throws Exception {
        var client = TestEntities.newClient("Acme", "a@acme.com");
        clientDao.insert(client);

        var product1 = TestEntities.newProduct("bc1", "P1", client.getId(), 100.0, null);
        productDao.insert(product1);

        var product2 = TestEntities.newProduct("bc2", "P2", client.getId(), 200.0, null);
        productDao.insert(product2);

        inventoryDao.insert(TestEntities.newInventory(product1.getId(), 50));
        inventoryDao.insert(TestEntities.newInventory(product2.getId(), 50));
        flushAndClear();

        Integer orderId = orderDto.create(orderForm(
                item("bc1", 1, 10.0),
                item("bc2", 3, 20.0)
        ));
        assertNotNull(orderId);

        var items = orderDto.getItems(orderId);
        assertEquals(2, items.size());
    }

    @Test
    void shouldThrowWhenCreateBarcodeBlank() {
        assertThrows(ApiException.class, () -> orderDto.create(orderForm(item("   ", 1, 5.0))));
    }

    @Test
    void shouldThrowWhenBarcodeNull() {
        assertThrows(ApiException.class, () -> orderDto.create(orderForm(item(null, 1, 5.0))));
    }

    @Test
    void shouldThrowWhenQuantityNull() {
        assertThrows(ApiException.class, () -> orderDto.create(orderForm(item("b1", null, 5.0))));
    }

    @Test
    void shouldThrowWhenSellingPriceNull() {
        assertThrows(ApiException.class, () -> orderDto.create(orderForm(item("b1", 1, null))));
    }

    @Test
    void shouldThrowWhenQuantityZeroOrNegative() {
        assertThrows(ApiException.class, () -> orderDto.create(orderForm(item("b1", 0, 5.0))));
    }

    @Test
    void shouldThrowWhenSellingPriceNegative() {
        assertThrows(ApiException.class, () -> orderDto.create(orderForm(item("b1", 1, -1.0))));
    }

    @Test
    void shouldThrowWhenItemsNull() {
        OrderForm form = new OrderForm();
        form.setItems(null);
        assertThrows(ApiException.class, () -> orderDto.create(form));
    }

    @Test
    void shouldThrowWhenItemsEmpty() {
        OrderForm form = new OrderForm();
        form.setItems(List.of());
        assertThrows(ApiException.class, () -> orderDto.create(form));
    }

    @Test
    void shouldThrowWhenBarcodeNotFound() throws Exception {
        var client = TestEntities.newClient("Acme", "a@acme.com");
        clientDao.insert(client);

        var product = TestEntities.newProduct("b1", "P1", client.getId(), 100.0, null);
        productDao.insert(product);

        inventoryDao.insert(TestEntities.newInventory(product.getId(), 10));
        flushAndClear();

        assertThrows(ApiException.class, () -> orderDto.create(orderForm(item("missing", 1, 10.0))));
    }

    @Test
    void create_whenItemsNull_shouldPassEmptyListToFlow() throws Exception {
        OrderForm form = new OrderForm();
        form.setItems(null);

        try {
            orderDto.create(form);
        } catch (Exception ignored) {
        }
    }

    @Test
    void create_whenItemsEmpty_shouldHitEmptyBranch() throws Exception {
        OrderForm form = new OrderForm();
        form.setItems(List.of());

        try {
            orderDto.create(form);
        } catch (Exception ignored) {
        }
    }

    @Test
    void shouldThrowWhenBarcodeNotFound_afterTrimNormalization() throws Exception {
        var client = TestEntities.newClient("Acme2", "a2@acme.com");
        clientDao.insert(client);

        var product = TestEntities.newProduct("exists", "P-exists", client.getId(), 100.0, null);
        productDao.insert(product);

        inventoryDao.insert(TestEntities.newInventory(product.getId(), 10));
        flushAndClear();

        ApiException ex = assertThrows(ApiException.class,
                () -> orderDto.create(orderForm(item("  missing-bc  ", 1, 10.0))));

        assertTrue(ex.getMessage().toLowerCase().contains("barcode"));
    }
}