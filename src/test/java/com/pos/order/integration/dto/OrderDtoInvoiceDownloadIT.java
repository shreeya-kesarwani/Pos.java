package com.pos.order.integration.dto;

import com.pos.api.OrderApi;
import com.pos.client.InvoiceClient;
import com.pos.dao.ClientDao;
import com.pos.dao.InventoryDao;
import com.pos.dao.ProductDao;
import com.pos.dto.OrderDto;
import com.pos.exception.ApiException;
import com.pos.model.data.InvoiceData;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.pojo.Order;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestEntities;
import com.pos.utils.InvoicePathUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderDtoInvoiceDownloadIT extends AbstractIntegrationTest {

    private static final String INVOICE_DIR = "/tmp/invoices";

    @Autowired private OrderDto orderDto;
    @Autowired private OrderApi orderApi;

    @Autowired private ClientDao clientDao;
    @Autowired private ProductDao productDao;
    @Autowired private InventoryDao inventoryDao;

    @MockBean private InvoiceClient invoiceClient;

    @Test
    void shouldDownloadInvoice_useExistingFileAndNotCallInvoiceClient() throws Exception {
        Integer orderId = createOrderWithOneItem("b1");

        byte[] existingBytes = "existing-invoice".getBytes();
        Files.createDirectories(Path.of(INVOICE_DIR));
        Path invoiceFile = InvoicePathUtil.invoiceFilePath(INVOICE_DIR, orderId);
        Files.write(invoiceFile, existingBytes);

        Order order = orderApi.getCheck(orderId);
        order.setInvoicePath(INVOICE_DIR);
        flushAndClear();

        ResponseEntity<byte[]> resp = orderDto.downloadInvoice(orderId);

        assertNotNull(resp.getBody());
        assertArrayEquals(existingBytes, resp.getBody());
        verify(invoiceClient, never()).generate(any());
    }

    @Test
    void shouldGenerateInvoiceWhenDownloadingWithoutExistingPath() throws Exception {
        Integer orderId = createOrderWithOneItem("b2");

        Files.deleteIfExists(InvoicePathUtil.invoiceFilePath(INVOICE_DIR, orderId));

        byte[] pdf = "pdf".getBytes();
        InvoiceData data = new InvoiceData();
        data.setOrderId(orderId);
        data.setBase64Pdf(Base64.getEncoder().encodeToString(pdf));

        when(invoiceClient.generate(any())).thenReturn(data);

        ResponseEntity<byte[]> resp = orderDto.downloadInvoice(orderId);

        assertNotNull(resp.getBody());
        assertArrayEquals(pdf, resp.getBody());
        verify(invoiceClient, times(1)).generate(any());

        assertTrue(Files.exists(InvoicePathUtil.invoiceFilePath(INVOICE_DIR, orderId)));
    }

    @Test
    void shouldGenerateInvoiceWhenInvoicePathPresentButFileMissing() throws Exception {
        Integer orderId = createOrderWithOneItem("b3");

        Order order = orderApi.getCheck(orderId);
        order.setInvoicePath(INVOICE_DIR);
        flushAndClear();

        Files.deleteIfExists(InvoicePathUtil.invoiceFilePath(INVOICE_DIR, orderId));

        byte[] pdf = "regen".getBytes();
        InvoiceData data = new InvoiceData();
        data.setOrderId(orderId);
        data.setBase64Pdf(Base64.getEncoder().encodeToString(pdf));

        when(invoiceClient.generate(any())).thenReturn(data);

        ResponseEntity<byte[]> resp = orderDto.downloadInvoice(orderId);

        assertNotNull(resp.getBody());
        assertArrayEquals(pdf, resp.getBody());
        verify(invoiceClient, times(1)).generate(any());
    }

    @Test
    void shouldThrowWhenDownloadInvoiceOrderIdNull() {
        ApiException ex = assertThrows(ApiException.class, () -> orderDto.downloadInvoice(null));
        assertTrue(ex.getMessage().contains("Order id is required"));
    }

    @Test
    void shouldRegenerateWhenInvoicePathIsBlank() throws Exception {
        Integer orderId = createOrderWithOneItem("b4");

        Order order = orderApi.getCheck(orderId);
        order.setInvoicePath("   ");
        flushAndClear();

        byte[] pdf = "regen-blank".getBytes();
        InvoiceData data = new InvoiceData();
        data.setOrderId(orderId);
        data.setBase64Pdf(Base64.getEncoder().encodeToString(pdf));

        when(invoiceClient.generate(any())).thenReturn(data);

        ResponseEntity<byte[]> resp = orderDto.downloadInvoice(orderId);

        assertNotNull(resp.getBody());
        assertArrayEquals(pdf, resp.getBody());
        verify(invoiceClient, times(1)).generate(any());
    }

    @Test
    void shouldGenerateWhenInvoicePathBlankString() throws Exception {
        Integer orderId = createOrderWithOneItem("bb1");

        Order order = orderApi.getCheck(orderId);
        order.setInvoicePath("   ");
        flushAndClear();

        byte[] pdf = "blank-path".getBytes();
        InvoiceData data = new InvoiceData();
        data.setOrderId(orderId);
        data.setBase64Pdf(Base64.getEncoder().encodeToString(pdf));
        when(invoiceClient.generate(any())).thenReturn(data);

        var resp = orderDto.downloadInvoice(orderId);

        assertArrayEquals(pdf, resp.getBody());
        verify(invoiceClient, times(1)).generate(any());
    }

    @Test
    void downloadInvoice_readsExistingFileWhenInvoicePathPresent() throws Exception {
        Integer orderId = createOrderWithOneItem("x1");

        Files.createDirectories(Path.of(INVOICE_DIR));
        byte[] existing = "hello".getBytes();
        Path file = InvoicePathUtil.invoiceFilePath(INVOICE_DIR, orderId);
        Files.write(file, existing);

        Order order = orderApi.getCheck(orderId);
        order.setInvoicePath(INVOICE_DIR);
        flushAndClear();

        var resp = orderDto.downloadInvoice(orderId);

        assertArrayEquals(existing, resp.getBody());
        verify(invoiceClient, never()).generate(any());
    }

    @Test
    void downloadInvoice_regeneratesWhenInvoicePathPresentButFileMissing() throws Exception {
        Integer orderId = createOrderWithOneItem("x2");

        Order order = orderApi.getCheck(orderId);
        order.setInvoicePath(INVOICE_DIR);
        flushAndClear();

        Files.deleteIfExists(InvoicePathUtil.invoiceFilePath(INVOICE_DIR, orderId));

        byte[] pdf = "regen".getBytes();
        InvoiceData data = new InvoiceData();
        data.setOrderId(orderId);
        data.setBase64Pdf(Base64.getEncoder().encodeToString(pdf));
        when(invoiceClient.generate(any())).thenReturn(data);

        var resp = orderDto.downloadInvoice(orderId);

        assertArrayEquals(pdf, resp.getBody());
        verify(invoiceClient, times(1)).generate(any());
    }

    @Test
    void downloadInvoice_generatesWhenInvoicePathIsNull() throws Exception {
        Integer orderId = createOrderWithOneItem("x3");

        Order order = orderApi.getCheck(orderId);
        order.setInvoicePath(null);
        flushAndClear();

        byte[] pdf = "new".getBytes();
        InvoiceData data = new InvoiceData();
        data.setOrderId(orderId);
        data.setBase64Pdf(Base64.getEncoder().encodeToString(pdf));
        when(invoiceClient.generate(any())).thenReturn(data);

        var resp = orderDto.downloadInvoice(orderId);

        assertArrayEquals(pdf, resp.getBody());
        verify(invoiceClient, times(1)).generate(any());
    }

    @Test
    void downloadInvoice_throwsWhenOrderIdNull() {
        assertThrows(ApiException.class, () -> orderDto.downloadInvoice(null));
    }

    @Test
    void downloadInvoice_whenInvoicePathEmptyString_shouldGenerate() throws Exception {
        Integer orderId = createOrderWithOneItem("z1");

        Order order = orderApi.getCheck(orderId);
        order.setInvoicePath("");
        flushAndClear();

        byte[] pdf = "x".getBytes();
        InvoiceData data = new InvoiceData();
        data.setOrderId(orderId);
        data.setBase64Pdf(Base64.getEncoder().encodeToString(pdf));
        when(invoiceClient.generate(any())).thenReturn(data);

        var resp = orderDto.downloadInvoice(orderId);

        assertArrayEquals(pdf, resp.getBody());
        verify(invoiceClient, times(1)).generate(any());
    }

    @Test
    void downloadInvoice_whenInvoicePathBlank_shouldGenerate() throws Exception {
        Integer orderId = createOrderWithOneItem("bblank");

        Order order = orderApi.getCheck(orderId);
        order.setInvoicePath("   ");
        flushAndClear();

        byte[] pdf = "x".getBytes();
        InvoiceData data = new InvoiceData();
        data.setOrderId(orderId);
        data.setBase64Pdf(Base64.getEncoder().encodeToString(pdf));
        when(invoiceClient.generate(any())).thenReturn(data);

        var resp = orderDto.downloadInvoice(orderId);

        assertArrayEquals(pdf, resp.getBody());
        verify(invoiceClient, times(1)).generate(any());
    }

    // ---------- helpers ----------

    private Integer createOrderWithOneItem(String barcode) throws Exception {
        var client = TestEntities.newClient("Acme-" + barcode, barcode + "@acme.com");
        clientDao.insert(client);

        var product = TestEntities.newProduct(barcode, "P-" + barcode, client.getId(), 100.0, null);
        productDao.insert(product);

        inventoryDao.insert(TestEntities.newInventory(product.getId(), 50));
        flushAndClear();

        OrderItemForm item = new OrderItemForm();
        item.setBarcode(barcode);
        item.setQuantity(1);
        item.setSellingPrice(5.0);

        OrderForm orderForm = new OrderForm();
        orderForm.setItems(List.of(item));

        Integer orderId = orderDto.create(orderForm);
        flushAndClear();
        return orderId;
    }
}