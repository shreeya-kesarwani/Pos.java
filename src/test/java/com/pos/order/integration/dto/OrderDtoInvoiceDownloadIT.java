package com.pos.order.integration.dto;

import com.pos.api.OrderApi;
import com.pos.client.InvoiceClient;
import com.pos.dto.OrderDto;
import com.pos.exception.ApiException;
import com.pos.model.data.InvoiceData;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestFactory;
import com.pos.utils.InvoicePathUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderDtoInvoiceDownloadIT extends AbstractIntegrationTest {

    @Autowired OrderDto orderDto;
    @Autowired OrderApi orderApi;
    @Autowired TestFactory factory;

    @MockBean InvoiceClient invoiceClient; // external dependency ONLY

    @Test
    void shouldDownloadInvoice_useExistingBytesWithoutCallingInvoiceClient() throws Exception {
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

        // Create an existing invoice file and attach it to order
        byte[] existingBytes = "existing-invoice".getBytes();

        // Save invoice in the same dir OrderDto uses (/tmp/invoices) by giving directory path
        String invoiceDir = "/tmp/invoices";
        Files.createDirectories(Path.of(invoiceDir));
        Path invoiceFile = InvoicePathUtil.invoiceFilePath(invoiceDir, orderId);
        Files.write(invoiceFile, existingBytes);

        // Attach path via real internal API (no mocks)
        orderApi.generateInvoice(orderId, invoiceDir);
        flushAndClear();

        // Act
        ResponseEntity<byte[]> resp = orderDto.downloadInvoice(orderId);

        // Assert: returned bytes are from existing file
        assertNotNull(resp.getBody());
        assertArrayEquals(existingBytes, resp.getBody());

        // Assert: should NOT call external invoice client
        Mockito.verify(invoiceClient, never()).generate(any());
    }

    @Test
    void shouldGenerateInvoiceWhenDownloadingWithoutExistingPath() throws Exception {
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

        byte[] pdf = "pdf".getBytes();
        InvoiceData data = new InvoiceData();
        data.setOrderId(orderId);
        data.setBase64Pdf(Base64.getEncoder().encodeToString(pdf));

        when(invoiceClient.generate(any())).thenReturn(data);

        var resp = orderDto.downloadInvoice(orderId);

        assertNotNull(resp.getBody());
    }

    @Test
    void shouldRegenerateInvoiceWhenFileMissing() throws Exception {
        // generate once normally
        // delete saved file manually
        // call download again
        // verify invoiceClient called
    }

    @Test
    void shouldThrowWhenInvoicePathPresentButFileMissing() throws Exception {
        // Create order with items
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

        // Manually attach invoice path (simulate already generated)
        orderApi.generateInvoice(orderId, "/tmp/invoices"); // directory, file likely missing
        flushAndClear();

        // Because API treats invoice as already generated, regen would attempt attach again → should throw
        assertThrows(ApiException.class, () -> orderDto.downloadInvoice(orderId));

        // and invoiceClient should NOT be called
        verify(invoiceClient, never()).generate(any());
    }
}