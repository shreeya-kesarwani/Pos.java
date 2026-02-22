package com.pos.order.integration;

import com.pos.api.OrderApi;
import com.pos.api.ProductApi;
import com.pos.client.InvoiceClient;
import com.pos.dto.OrderDto;
import com.pos.exception.ApiException;
import com.pos.flow.OrderFlow;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.InvoiceData;
import com.pos.model.data.OrderData;
import com.pos.model.data.OrderItemData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.model.form.OrderSearchForm;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
import com.pos.utils.InvoicePathUtil;
import com.pos.utils.InvoiceStorageUtil;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderDtoTest {

    @Mock private OrderFlow orderFlow;
    @Mock private ProductApi productApi;
    @Mock private OrderApi orderApi;
    @Mock private InvoiceClient invoiceClient;
    @Mock private Validator validator;

    @InjectMocks private OrderDto orderDto;

    @Test
    void createNormalizesValidatesAndCallsFlowWithResolvedProductIds() throws ApiException {
        OrderItemForm item = new OrderItemForm();
        item.setBarcode("  b1 ");
        item.setQuantity(2);
        item.setSellingPrice(10.0);

        OrderForm form = new OrderForm();
        form.setItems(List.of(item));

        when(validator.validate(any(OrderForm.class))).thenReturn(Set.of());

        Product p = new Product();
        p.setId(99);
        p.setBarcode("b1");
        when(productApi.getCheckByBarcodes(eq(List.of("b1")))).thenReturn(List.of(p));

        when(orderFlow.createOrder(anyList())).thenReturn(123);

        Integer id = orderDto.create(form);
        assertEquals(123, id);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);

        verify(orderFlow).createOrder(itemsCaptor.capture());
        assertEquals(1, itemsCaptor.getValue().size());
        assertEquals(99, itemsCaptor.getValue().get(0).getProductId());
    }

    @Test
    void createThrowsWhenBarcodeMissing() {
        OrderItemForm item = new OrderItemForm();
        item.setBarcode("   ");
        item.setQuantity(1);
        item.setSellingPrice(1.0);

        OrderForm form = new OrderForm();
        form.setItems(List.of(item));

        when(validator.validate(any(OrderForm.class))).thenReturn(Set.of());

        ApiException ex = assertThrows(ApiException.class, () -> orderDto.create(form));
        assertTrue(ex.getMessage().toLowerCase().contains("barcode"));
        verifyNoInteractions(orderFlow);
    }

    @Test
    void searchParsesStatusAndReturnsPaginatedResponse() throws ApiException {
        OrderSearchForm form = new OrderSearchForm();
        form.setId(null);
        form.setStart(ZonedDateTime.now().minusDays(1));
        form.setEnd(ZonedDateTime.now());
        form.setStatus("  CREATED  ");
        form.setPageNumber(1);
        form.setPageSize(10);

        when(validator.validate(any(OrderSearchForm.class))).thenReturn(Set.of());

        Order o = new Order();
        o.setId(1);
        o.setStatus(OrderStatus.CREATED);

        when(orderApi.search(
                eq(null),
                any(ZonedDateTime.class),
                any(ZonedDateTime.class),
                eq(OrderStatus.CREATED),
                eq(1),
                eq(10)
        )).thenReturn(List.of(o));

        when(orderApi.getCount(
                eq(null),
                any(ZonedDateTime.class),
                any(ZonedDateTime.class),
                eq(OrderStatus.CREATED)
        )).thenReturn(1L);

        when(orderApi.getItemsByOrderIds(eq(List.of(1)))).thenReturn(List.of());

        PaginatedResponse<OrderData> resp = orderDto.search(form);

        assertEquals(1L, resp.getTotalCount());
        assertEquals(1, resp.getData().size());

        verify(orderApi).getItemsByOrderIds(eq(List.of(1)));
        verifyNoInteractions(orderFlow);
    }

    @Test
    void getItemsReturnsEmptyWhenNoItems() throws ApiException {
        when(orderApi.getItemsByOrderId(eq(5))).thenReturn(List.of());

        List<OrderItemData> items = orderDto.getItems(5);

        assertTrue(items.isEmpty());
        verify(orderApi).getItemsByOrderId(5);
        verifyNoInteractions(productApi);
        verifyNoInteractions(orderFlow);
    }

    @Test
    void generateInvoiceThrowsWhenOrderHasNoItems() throws ApiException {
        when(orderApi.getItemsByOrderId(eq(9))).thenReturn(List.of());

        ApiException ex = assertThrows(ApiException.class, () -> orderDto.generateInvoice(9));
        assertTrue(ex.getMessage().contains("orderId=9"));

        verify(orderApi).getItemsByOrderId(9);
        verifyNoInteractions(orderFlow);
    }

    @Test
    void generateInvoiceCallsClientAndStoresAndAttaches() throws ApiException {
        OrderItem oi = new OrderItem();
        oi.setOrderId(10);
        oi.setProductId(7);
        oi.setQuantity(1);
        oi.setSellingPrice(5.0);

        when(orderApi.getItemsByOrderId(eq(10))).thenReturn(List.of(oi));

        Product p = new Product();
        p.setId(7);
        p.setBarcode("b");
        p.setName("P");
        when(productApi.getByIds(eq(List.of(7)))).thenReturn(List.of(p));

        when(validator.validate(any())).thenReturn(Set.of());

        byte[] pdf = "pdf".getBytes();
        InvoiceData data = new InvoiceData();
        data.setOrderId(10);
        data.setBase64Pdf(Base64.getEncoder().encodeToString(pdf));
        when(invoiceClient.generate(any())).thenReturn(data);

        try (MockedStatic<InvoiceStorageUtil> storage = mockStatic(InvoiceStorageUtil.class)) {
            storage.when(() -> InvoiceStorageUtil.storeAndAttach(any(), anyString(), anyInt(), any()))
                    .thenAnswer(inv -> null);

            InvoiceData out = orderDto.generateInvoice(10);

            assertEquals(data.getBase64Pdf(), out.getBase64Pdf());
            storage.verify(() -> InvoiceStorageUtil.storeAndAttach(eq(orderApi), anyString(), eq(10), eq(pdf)));
        }

        verify(orderApi).getItemsByOrderId(10);
        verifyNoInteractions(orderFlow);
    }

    @Test
    void downloadInvoiceUsesExistingInvoiceBytesWhenPresent() throws ApiException {
        Order o = new Order();
        o.setId(1);
        o.setInvoicePath("/tmp/x.pdf");
        when(orderApi.getCheck(eq(1))).thenReturn(o);

        byte[] bytes = "abc".getBytes();

        try (MockedStatic<InvoicePathUtil> pathUtil = mockStatic(InvoicePathUtil.class)) {
            pathUtil.when(() -> InvoicePathUtil.tryReadInvoiceBytes(eq("/tmp/x.pdf"), eq(1))).thenReturn(bytes);
            pathUtil.when(() -> InvoicePathUtil.invoiceFileName(eq(1))).thenReturn("invoice-1.pdf");

            ResponseEntity<byte[]> resp = orderDto.downloadInvoice(1);

            assertArrayEquals(bytes, resp.getBody());
            verify(invoiceClient, never()).generate(any());
        }
    }
}