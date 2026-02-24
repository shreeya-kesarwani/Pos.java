package com.pos.order.integration.dto;

import com.pos.api.OrderApi;
import com.pos.client.InvoiceClient;
import com.pos.dto.OrderDto;
import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.InvoiceData;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestEntities;
import com.pos.setup.TestFactory;
import com.pos.utils.InvoicePathUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

    @Autowired OrderDto orderDto;
    @Autowired OrderApi orderApi;
    @Autowired TestFactory factory;

    @MockBean InvoiceClient invoiceClient; // external dependency ONLY

    @Test
    void shouldGenerateInvoice_storePdfAndAttachPath_happyFlow() throws Exception {
        // Arrange: create product + inventory + order
        var client = factory.createClient("Acme", "a@acme.com");
        var product = factory.createProduct("b1", "P1", client.getId(), 100.0, null);
        factory.createInventory(product.getId(), 50);
        flushAndClear();

        OrderItemForm item = new OrderItemForm();
        item.setBarcode("b1");
        item.setQuantity(1);
        item.setSellingPrice(5.0);

        OrderForm orderForm = new OrderForm();
        orderForm.setItems(List.of(item));

        Integer orderId = orderDto.create(orderForm);
        flushAndClear();

        // Mock external invoice generation
        byte[] pdfBytes = "pdf-bytes".getBytes();
        InvoiceData invoiceData = new InvoiceData();
        invoiceData.setOrderId(orderId);
        invoiceData.setBase64Pdf(Base64.getEncoder().encodeToString(pdfBytes));

        when(invoiceClient.generate(any())).thenReturn(invoiceData);

        // Act
        InvoiceData out = orderDto.generateInvoice(orderId);
        flushAndClear();

        // Assert: invoice client called
        verify(invoiceClient).generate(any());
        assertEquals(invoiceData.getBase64Pdf(), out.getBase64Pdf());

        // Assert: order has invoice path attached
        var updatedOrder = orderApi.getCheck(orderId);
        assertNotNull(updatedOrder.getInvoicePath());
        assertTrue(updatedOrder.getInvoicePath().endsWith(InvoicePathUtil.invoiceFileName(orderId)));

        // Assert: file exists and matches bytes
        Path savedPath = Path.of(updatedOrder.getInvoicePath());
        assertTrue(Files.exists(savedPath));
        assertArrayEquals(pdfBytes, Files.readAllBytes(savedPath));
    }

    @Test
    void shouldThrowWhenGenerateInvoiceNoItems() {
        var order = factory.createOrder(OrderStatus.CREATED, null);
        flushAndClear();

        assertThrows(ApiException.class, () -> orderDto.generateInvoice(order.getId()));
    }

    @Test
    void shouldThrowWhenGenerateInvoiceOrderIdNull() {
        ApiException ex = assertThrows(ApiException.class, () -> orderDto.generateInvoice(null));
        assertTrue(ex.getMessage().contains("Order id is required"));
    }

    @Test
    void shouldThrowWhenGenerateInvoiceForEmptyOrder() throws Exception {
        var order = factory.createOrder(com.pos.model.constants.OrderStatus.CREATED, null);
        flushAndClear();

        assertThrows(ApiException.class, () -> orderDto.generateInvoice(order.getId()));
    }

    @Test
    void shouldThrowWhenGenerateInvoiceProductMissing() throws Exception {
        var order = factory.createOrder(com.pos.model.constants.OrderStatus.CREATED, null);

        // orderItem references non-existent productId
        factory.createOrderItems(order.getId(),
                List.of(com.pos.setup.TestEntities.orderItem(order.getId(), 999999, 1, 10.0))
        );
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
    void generateInvoice_whenOrderHasNoItems_shouldThrow() throws Exception {
        var order = factory.createOrder(OrderStatus.CREATED, null);
        flushAndClear();

        assertThrows(ApiException.class, () -> orderDto.generateInvoice(order.getId()));
    }

    @Test
    void generateInvoice_whenProductMissing_shouldThrow() throws Exception {
        var order = factory.createOrder(OrderStatus.CREATED, null);

        factory.createOrderItems(order.getId(),
                List.of(TestEntities.orderItem(order.getId(), 999999, 1, 10.0))
        );

        flushAndClear();

        assertThrows(ApiException.class, () -> orderDto.generateInvoice(order.getId()));
    }

    private Integer createOrderWithOneItem(String barcode) throws Exception {
        var client = factory.createClient("C-" + barcode, barcode + "@acme.com");
        var product = factory.createProduct(barcode, "P-" + barcode, client.getId(), 100.0, null);
        factory.createInventory(product.getId(), 50);
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