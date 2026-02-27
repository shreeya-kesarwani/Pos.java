package com.pos.order.unit;

import com.pos.api.OrderApi;
import com.pos.dao.OrderDao;
import com.pos.dao.OrderItemDao;
import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;

import static com.pos.model.constants.ErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApiTest {

    @InjectMocks
    private OrderApi orderApi;

    @Mock private OrderDao orderDao;
    @Mock private OrderItemDao orderItemDao;

    private ZonedDateTime start;
    private ZonedDateTime end;

    private OrderItem item(Integer productId, Integer qty, Double sp) {
        OrderItem oi = new OrderItem();
        oi.setProductId(productId);
        oi.setQuantity(qty);
        oi.setSellingPrice(sp);
        return oi;
    }

    private Order order(Integer id, OrderStatus status, String invoicePath) {
        Order o = new Order();
        o.setId(id);
        o.setStatus(status);
        o.setInvoicePath(invoicePath);
        return o;
    }

    @BeforeEach
    void setupData() {
        start = ZonedDateTime.now().minusDays(1);
        end = ZonedDateTime.now();
    }

    @Test
    void createShouldThrowWhenItemsNull() {
        ApiException ex = assertThrows(ApiException.class, () -> orderApi.create(null));
        assertTrue(ex.getMessage().contains(NO_ORDER_ITEMS_FOUND.value()));
        verifyNoInteractions(orderDao, orderItemDao);
    }

    @Test
    void createShouldThrowWhenItemsEmpty() {
        ApiException ex = assertThrows(ApiException.class, () -> orderApi.create(List.of()));
        assertTrue(ex.getMessage().contains(NO_ORDER_ITEMS_FOUND.value()));
        verifyNoInteractions(orderDao, orderItemDao);
    }

    @Test
    void createShouldThrowWhenProductIdNull() {
        ApiException ex = assertThrows(ApiException.class, () -> orderApi.create(List.of(item(null, 1, 10.0))));
        assertTrue(ex.getMessage().contains(PRODUCT_NOT_FOUND.value()));
        verifyNoInteractions(orderDao, orderItemDao);
    }

    @Test
    void createShouldThrowWhenQuantityNullOrNonPositive() {
        assertTrue(assertThrows(ApiException.class,
                () -> orderApi.create(List.of(item(1, null, 10.0))))
                .getMessage().contains(QUANTITY_MUST_BE_POSITIVE.value()));

        assertTrue(assertThrows(ApiException.class,
                () -> orderApi.create(List.of(item(1, 0, 10.0))))
                .getMessage().contains(QUANTITY_MUST_BE_POSITIVE.value()));

        assertTrue(assertThrows(ApiException.class,
                () -> orderApi.create(List.of(item(1, -1, 10.0))))
                .getMessage().contains(QUANTITY_MUST_BE_POSITIVE.value()));

        verifyNoInteractions(orderDao, orderItemDao);
    }

    @Test
    void createShouldThrowWhenSellingPriceNullOrNegative() {
        assertTrue(assertThrows(ApiException.class,
                () -> orderApi.create(List.of(item(1, 1, null))))
                .getMessage().contains(SELLING_PRICE_CANNOT_BE_NEGATIVE.value()));

        assertTrue(assertThrows(ApiException.class,
                () -> orderApi.create(List.of(item(1, 1, -0.01))))
                .getMessage().contains(SELLING_PRICE_CANNOT_BE_NEGATIVE.value()));

        verifyNoInteractions(orderDao, orderItemDao);
    }

    @Test
    void createShouldInsertOrderThenInsertItemsAndReturnOrderId() throws ApiException {
        // Important: insert sets the id on the managed entity (simulate DB behavior)
        doAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(999);
            return null;
        }).when(orderDao).insert(any(Order.class));

        OrderItem i1 = item(11, 2, 10.0);
        OrderItem i2 = item(22, 1, 5.0);

        Integer orderId = orderApi.create(List.of(i1, i2));

        assertEquals(999, orderId);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderDao).insert(orderCaptor.capture());
        assertEquals(OrderStatus.CREATED, orderCaptor.getValue().getStatus());

        // API should assign orderId into each item before insert
        assertEquals(999, i1.getOrderId());
        assertEquals(999, i2.getOrderId());

        verify(orderItemDao).insert(i1);
        verify(orderItemDao).insert(i2);

        verifyNoMoreInteractions(orderDao, orderItemDao);
    }

    @Test
    void getCheckShouldReturnOrderWhenFound() throws ApiException {
        Order o = order(10, OrderStatus.CREATED, null);
        when(orderDao.selectById(10)).thenReturn(o);

        Order out = orderApi.getCheck(10);

        assertSame(o, out);
        verify(orderDao).selectById(10);
        verifyNoMoreInteractions(orderDao);
        verifyNoInteractions(orderItemDao);
    }

    @Test
    void getCheckShouldThrowWhenNotFound() {
        when(orderDao.selectById(10)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> orderApi.getCheck(10));
        assertTrue(ex.getMessage().contains(ORDER_NOT_FOUND.value()));
        assertTrue(ex.getMessage().contains("10"));

        verify(orderDao).selectById(10);
        verifyNoMoreInteractions(orderDao);
        verifyNoInteractions(orderItemDao);
    }

    @Test
    void generateInvoiceShouldThrowWhenPathNullOrBlank() {
        ApiException ex1 = assertThrows(ApiException.class, () -> orderApi.generateInvoice(1, null));
        assertTrue(ex1.getMessage().contains(INVOICE_PATH_REQUIRED.value()));

        ApiException ex2 = assertThrows(ApiException.class, () -> orderApi.generateInvoice(1, "   "));
        assertTrue(ex2.getMessage().contains(INVOICE_PATH_REQUIRED.value()));

        verifyNoInteractions(orderDao, orderItemDao);
    }

    @Test
    void generateInvoiceShouldThrowWhenOrderNotFound() {
        when(orderDao.selectById(1)).thenReturn(null);

        assertThrows(ApiException.class, () -> orderApi.generateInvoice(1, "/tmp/inv.pdf"));

        verify(orderDao).selectById(1);
        verifyNoMoreInteractions(orderDao);
        verifyNoInteractions(orderItemDao);
    }

    @Test
    void generateInvoiceShouldSetInvoicePathAndStatusInvoiced() throws ApiException {
        Order o = order(1, OrderStatus.CREATED, null);
        when(orderDao.selectById(1)).thenReturn(o);

        orderApi.generateInvoice(1, "/tmp/inv.pdf");

        assertEquals("/tmp/inv.pdf", o.getInvoicePath());
        assertEquals(OrderStatus.INVOICED, o.getStatus());

        verify(orderDao).selectById(1);
        verifyNoMoreInteractions(orderDao);
        verifyNoInteractions(orderItemDao);
    }

    @Test
    void searchShouldCallDaoSearch() {
        List<Order> expected = List.of(order(1, OrderStatus.CREATED, null));
        when(orderDao.search(1, start, end, OrderStatus.CREATED, 0, 10)).thenReturn(expected);

        List<Order> out = orderApi.search(1, start, end, OrderStatus.CREATED, 0, 10);

        assertSame(expected, out);
        verify(orderDao).search(1, start, end, OrderStatus.CREATED, 0, 10);
        verifyNoMoreInteractions(orderDao);
        verifyNoInteractions(orderItemDao);
    }

    @Test
    void getCountShouldCallDaoGetCount() {
        when(orderDao.getCount(1, start, end, OrderStatus.INVOICED)).thenReturn(123L);

        Long out = orderApi.getCount(1, start, end, OrderStatus.INVOICED);

        assertEquals(123L, out);
        verify(orderDao).getCount(1, start, end, OrderStatus.INVOICED);
        verifyNoMoreInteractions(orderDao);
        verifyNoInteractions(orderItemDao);
    }

    @Test
    void getItemsByOrderIdShouldCallDaos() throws ApiException {
        Order o = order(5, OrderStatus.CREATED, null);
        when(orderDao.selectById(5)).thenReturn(o);

        List<OrderItem> expected = List.of(item(1, 1, 10.0));
        when(orderItemDao.selectByOrderId(5)).thenReturn(expected);

        List<OrderItem> out = orderApi.getItemsByOrderId(5);

        assertSame(expected, out);
        verify(orderDao).selectById(5);
        verify(orderItemDao).selectByOrderId(5);
        verifyNoMoreInteractions(orderDao, orderItemDao);
    }

    @Test
    void getCheckItemsByOrderIdShouldThrowWhenNullOrEmpty() {
        when(orderItemDao.selectByOrderId(7)).thenReturn(null);
        ApiException ex1 = assertThrows(ApiException.class, () -> orderApi.getCheckItemsByOrderId(7));
        assertTrue(ex1.getMessage().contains(NO_ORDER_ITEMS_FOUND.value()));

        when(orderItemDao.selectByOrderId(8)).thenReturn(List.of());
        ApiException ex2 = assertThrows(ApiException.class, () -> orderApi.getCheckItemsByOrderId(8));
        assertTrue(ex2.getMessage().contains(NO_ORDER_ITEMS_FOUND.value()));

        verify(orderItemDao).selectByOrderId(7);
        verify(orderItemDao).selectByOrderId(8);
        verifyNoMoreInteractions(orderItemDao);
        verifyNoInteractions(orderDao);
    }

    @Test
    void getCheckItemsByOrderIdShouldReturnItemsWhenFound() throws ApiException {
        List<OrderItem> expected = List.of(item(1, 1, 10.0), item(2, 2, 20.0));
        when(orderItemDao.selectByOrderId(9)).thenReturn(expected);

        List<OrderItem> out = orderApi.getCheckItemsByOrderId(9);

        assertSame(expected, out);
        verify(orderItemDao).selectByOrderId(9);
        verifyNoMoreInteractions(orderItemDao);
        verifyNoInteractions(orderDao);
    }

    @Test
    void getItemsByOrderIdsShouldCallDao() {
        List<OrderItem> expected = List.of(item(1, 1, 10.0));
        when(orderItemDao.selectByOrderIds(List.of(1, 2))).thenReturn(expected);

        List<OrderItem> out = orderApi.getItemsByOrderIds(List.of(1, 2));

        assertSame(expected, out);
        verify(orderItemDao).selectByOrderIds(List.of(1, 2));
        verifyNoMoreInteractions(orderItemDao);
        verifyNoInteractions(orderDao);
    }
}