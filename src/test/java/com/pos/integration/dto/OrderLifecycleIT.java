package com.pos.integration.dto;

import com.pos.dao.ClientDao;
import com.pos.dto.OrderDto;
import com.pos.dto.ProductDto;
import com.pos.exception.ApiException;
import com.pos.integration.AbstractMySqlIntegrationTest;
import com.pos.model.data.OrderData;
import com.pos.model.data.OrderItemData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.model.form.ProductForm;
import com.pos.pojo.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderLifecycleIT extends AbstractMySqlIntegrationTest {

    @Autowired private OrderDto orderDto;
    @Autowired private ProductDto productDto;
    @Autowired private ClientDao clientDao;

    private void insertClient(String name, String email) {
        Client c = new Client();
        c.setName(name);
        c.setEmail(email);
        clientDao.insert(c);
    }

    private void addProduct(String clientName, String barcode, String name, double mrp) throws Exception {
        ProductForm f = new ProductForm();
        f.setClientName(clientName);
        f.setBarcode(barcode);
        f.setName(name);
        f.setMrp(mrp);
        f.setImageUrl(null);
        productDto.add(f);
    }

    private static OrderItemForm item(String barcode, int quantity, double sellingPrice) {
        OrderItemForm i = new OrderItemForm();
        i.setBarcode(barcode);
        i.setQuantity(quantity);
        i.setSellingPrice(sellingPrice);
        return i;
    }

    @Test
    void create_shouldCreateOrder_andItemsShouldBeFetchable() throws Exception {
        // Arrange: client + 2 products
        insertClient("ABC", "abc@test.com");
        addProduct("ABC", "OB1", "Pen", 10.0);
        addProduct("ABC", "OB2", "Pencil", 5.0);

        OrderForm form = new OrderForm();
        form.setItems(List.of(
                item("OB1", 2, 9.0),  // qty 2
                item("OB2", 3, 4.0)   // qty 3
        ));

        // Act: create order
        Integer orderId = orderDto.create(form);
        assertNotNull(orderId);

        // Assert: items exist and match
        List<OrderItemData> items = orderDto.getItems(orderId);
        assertNotNull(items);
        assertEquals(2, items.size());

        assertTrue(items.stream().anyMatch(i -> "OB1".equals(i.getBarcode()) && i.getQuantity() == 2));
        assertTrue(items.stream().anyMatch(i -> "OB2".equals(i.getBarcode()) && i.getQuantity() == 3));

        // Assert: order is searchable by id
        PaginatedResponse<OrderData> resp = orderDto.search(
                orderId,
                null,
                null,
                null,
                0,
                10
        );

        assertNotNull(resp);
        assertNotNull(resp.getData());
        assertEquals(1L, resp.getTotalCount());
        assertEquals(1, resp.getData().size());
        assertEquals(orderId, resp.getData().get(0).getId());

        // totalAmount is computed by flow; just sanity-check it's non-null
        assertNotNull(resp.getData().get(0).getTotalAmount());
        assertTrue(resp.getData().get(0).getTotalAmount() > 0.0);
    }

    @Test
    void create_shouldFail_whenNoItems() {
        OrderForm form = new OrderForm();
        form.setItems(List.of()); // empty

        ApiException ex = assertThrows(ApiException.class, () -> orderDto.create(form));
        assertTrue(ex.getMessage().contains("at least one item"));
    }

    @Test
    void search_shouldFilterByDateRange_basicSanity() throws Exception {
        // Arrange: create one order "now"
        insertClient("ABC", "abc@test.com");
        addProduct("ABC", "OB3", "Marker", 20.0);

        OrderForm form = new OrderForm();
        form.setItems(List.of(item("OB3", 1, 18.0)));
        Integer orderId = orderDto.create(form);

        // Act: search with a tight window around now
        ZonedDateTime end = ZonedDateTime.now();
        ZonedDateTime start = end.minusMinutes(5);

        PaginatedResponse<OrderData> resp = orderDto.search(
                null,
                start,
                end.plusMinutes(5),
                null,
                0,
                50
        );

        // Assert: should contain the newly created order (at least)
        assertNotNull(resp);
        assertNotNull(resp.getData());
        assertTrue(resp.getTotalCount() >= 1);
        assertTrue(resp.getData().stream().anyMatch(o -> orderId.equals(o.getId())));
    }
}
