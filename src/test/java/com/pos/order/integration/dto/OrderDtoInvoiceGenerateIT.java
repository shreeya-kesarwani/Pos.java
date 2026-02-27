package com.pos.order.integration.dto;

import com.pos.api.OrderApi;
import com.pos.client.InvoiceClient;
import com.pos.dao.*;
import com.pos.dto.OrderDto;
import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.InvoiceData;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestEntities;
import com.pos.utils.InvoicePathUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderDtoInvoiceGenerateIT extends AbstractIntegrationTest {

    @Autowired private OrderDto orderDto;
    @Autowired private OrderApi orderApi;

    @Autowired private ClientDao clientDao;
    @Autowired private ProductDao productDao;
    @Autowired private InventoryDao inventoryDao;
    @Autowired private OrderDao orderDao;
    @Autowired private OrderItemDao orderItemDao;

    @MockBean private InvoiceClient invoiceClient;

    @Test
    void shouldGenerateInvoice_storePdfAndAttachPath_happyFlow() throws Exception {
        var client = TestEntities.newClient("Acme", "a@acme.com");
        clientDao.insert(client);

        var product = TestEntities.newProduct("b1", "P1", client.getId(), 100.0, null);
        productDao.insert(product);

        inventoryDao.insert(TestEntities.newInventory(product.getId(), 50));
        flushAndClear();

        OrderItemForm item = new OrderItemForm();
        item.setBarcode("b1");
        item.setQuantity(1);
        item.setSellingPrice(5.0);

        OrderForm orderForm = new OrderForm();
        orderForm.setItems(List.of(item));

        Integer orderId = orderDto.create(orderForm);
        flushAndClear();

        byte[] pdfBytes = "pdf-bytes".getBytes();
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setOrderId(orderId);
        invoiceData.setBase64Pdf(Base64.getEncoder().encodeToString(pdfBytes));

        when(invoiceClient.generate(any())).thenReturn(invoiceData);

        InvoiceData out = orderDto.generateInvoice(orderId);
        flushAndClear();

        verify(invoiceClient).generate(any());
        assertEquals(invoiceData.getBase64Pdf(), out.getBase64Pdf());

        var updatedOrder = orderApi.getCheck(orderId);
        assertNotNull(updatedOrder.getInvoicePath());
        assertTrue(updatedOrder.getInvoicePath().endsWith(InvoicePathUtil.invoiceFileName(orderId)));

        Path savedPath = Path.of(updatedOrder.getInvoicePath());
        assertTrue(Files.exists(savedPath));
        assertArrayEquals(pdfBytes, Files.readAllBytes(savedPath));
    }

    @Test
    void shouldThrowWhenGenerateInvoiceNoItems() {
        var order = TestEntities.newOrder(OrderStatus.CREATED, null);
        orderDao.insert(order);
        flushAndClear();

        assertThrows(ApiException.class, () -> orderDto.generateInvoice(order.getId()));
    }

    @Test
    void shouldThrowWhenGenerateInvoiceOrderIdNull() {
        ApiException ex = assertThrows(ApiException.class, () -> orderDto.generateInvoice(null));
        assertTrue(ex.getMessage().contains("Order id is required"));
    }

    @Test
    void shouldThrowWhenGenerateInvoiceForEmptyOrder() {
        var order = TestEntities.newOrder(OrderStatus.CREATED, null);
        orderDao.insert(order);
        flushAndClear();

        assertThrows(ApiException.class, () -> orderDto.generateInvoice(order.getId()));
    }

    @Test
    void shouldThrowWhenGenerateInvoiceProductMissing() {
        var order = TestEntities.newOrder(OrderStatus.CREATED, null);
        orderDao.insert(order);

        // orderItem references non-existent productId
        var oi = TestEntities.newOrderItem(order.getId(), 999999, 1, 10.0);
        orderItemDao.insert(oi);

        flushAndClear();

        assertThrows(ApiException.class, () -> orderDto.generateInvoice(order.getId()));
    }

    @Test
    void generateInvoice_happyFlow() throws Exception {
        Integer orderId = createOrderWithOneItem("z2");

        byte[] pdf = "pdf".getBytes();
        InvoiceData data = new InvoiceData();
        data.setOrderId(orderId);
        data.setBase64Pdf(Base64.getEncoder().encodeToString(pdf));

        when(invoiceClient.generate(any())).thenReturn(data);

        var resp = orderDto.generateInvoice(orderId);

        assertNotNull(resp);
        verify(invoiceClient, times(1)).generate(any());
    }

    @Test
    void generateInvoice_whenOrderHasNoItems_shouldThrow() {
        var order = TestEntities.newOrder(OrderStatus.CREATED, null);
        orderDao.insert(order);
        flushAndClear();

        assertThrows(ApiException.class, () -> orderDto.generateInvoice(order.getId()));
    }

    @Test
    void generateInvoice_whenProductMissing_shouldThrow() {
        var order = TestEntities.newOrder(OrderStatus.CREATED, null);
        orderDao.insert(order);

        orderItemDao.insert(TestEntities.newOrderItem(order.getId(), 999999, 1, 10.0));
        flushAndClear();

        assertThrows(ApiException.class, () -> orderDto.generateInvoice(order.getId()));
    }

    private Integer createOrderWithOneItem(String barcode) throws Exception {
        var client = TestEntities.newClient("C-" + barcode, barcode + "@acme.com");
        clientDao.insert(client);

        var product = TestEntities.newProduct(barcode, "P-" + barcode, client.getId(), 100.0, null);
        productDao.insert(product);

        inventoryDao.insert(TestEntities.newInventory(product.getId(), 50));
        flushAndClear();

        OrderItemForm item = new OrderItemForm();
        item.setBarcode(barcode);
        item.setQuantity(1);
        item.setSellingPrice(10.0);

        OrderForm form = new OrderForm();
        form.setItems(List.of(item));

        Integer orderId = orderDto.create(form);
        flushAndClear();
        return orderId;
    }
}