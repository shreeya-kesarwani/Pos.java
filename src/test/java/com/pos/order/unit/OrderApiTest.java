package com.pos.order.unit;

import com.pos.api.OrderApi;
import com.pos.dao.OrderDao;
import com.pos.dao.OrderItemDao;
import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
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

    @Mock
    private OrderDao orderDao;

    @Mock
    private OrderItemDao orderItemDao;

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
        OrderItem item = new OrderItem();
        item.setProductId(null);
        item.setQuantity(1);
        item.setSellingPrice(10.0);

        ApiException ex = assertThrows(ApiException.class, () -> orderApi.create(List.of(item)));
        assertTrue(ex.getMessage().contains(PRODUCT_NOT_FOUND.value()));
        verifyNoInteractions(orderDao, orderItemDao);
    }

    @Test
    void createShouldThrowWhenQuantityNullOrNonPositive() {
        OrderItem item1 = new OrderItem();
        item1.setProductId(1);
        item1.setQuantity(null);
        item1.setSellingPrice(10.0);

        ApiException ex1 = assertThrows(ApiException.class, () -> orderApi.create(List.of(item1)));
        assertTrue(ex1.getMessage().contains(QUANTITY_MUST_BE_POSITIVE.value()));

        OrderItem item2 = new OrderItem();
        item2.setProductId(1);
        item2.setQuantity(0);
        item2.setSellingPrice(10.0);

        ApiException ex2 = assertThrows(ApiException.class, () -> orderApi.create(List.of(item2)));
        assertTrue(ex2.getMessage().contains(QUANTITY_MUST_BE_POSITIVE.value()));

        OrderItem item3 = new OrderItem();
        item3.setProductId(1);
        item3.setQuantity(-1);
        item3.setSellingPrice(10.0);

        ApiException ex3 = assertThrows(ApiException.class, () -> orderApi.create(List.of(item3)));
        assertTrue(ex3.getMessage().contains(QUANTITY_MUST_BE_POSITIVE.value()));

        verifyNoInteractions(orderDao, orderItemDao);
    }

    @Test
    void createShouldThrowWhenSellingPriceNullOrNegative() {
        OrderItem item1 = new OrderItem();
        item1.setProductId(1);
        item1.setQuantity(1);
        item1.setSellingPrice(null);

        ApiException ex1 = assertThrows(ApiException.class, () -> orderApi.create(List.of(item1)));
        assertTrue(ex1.getMessage().contains(SELLING_PRICE_CANNOT_BE_NEGATIVE.value()));

        OrderItem item2 = new OrderItem();
        item2.setProductId(1);
        item2.setQuantity(1);
        item2.setSellingPrice(-0.01);

        ApiException ex2 = assertThrows(ApiException.class, () -> orderApi.create(List.of(item2)));
        assertTrue(ex2.getMessage().contains(SELLING_PRICE_CANNOT_BE_NEGATIVE.value()));

        verifyNoInteractions(orderDao, orderItemDao);
    }

    @Test
    void createShouldInsertOrderThenInsertItemsAndReturnOrderId() throws ApiException {
        OrderItem i1 = new OrderItem();
        i1.setProductId(11);
        i1.setQuantity(2);
        i1.setSellingPrice(10.0);

        OrderItem i2 = new OrderItem();
        i2.setProductId(22);
        i2.setQuantity(1);
        i2.setSellingPrice(5.0);

        doAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(999);
            return null;
        }).when(orderDao).insert(any(Order.class));

        Integer orderId = orderApi.create(List.of(i1, i2));

        assertEquals(999, orderId);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderDao).insert(orderCaptor.capture());
        assertEquals(OrderStatus.CREATED, orderCaptor.getValue().getStatus());

        verify(orderItemDao).insert(i1);
        verify(orderItemDao).insert(i2);
        assertEquals(999, i1.getOrderId());
        assertEquals(999, i2.getOrderId());

        verifyNoMoreInteractions(orderDao, orderItemDao);
    }

    @Test
    void getCheckShouldReturnOrderWhenFound() throws ApiException {
        Order o = new Order();
        when(orderDao.selectById(10)).thenReturn(o);

        Order out = orderApi.getCheck(10);

        assertSame(o, out);
        verify(orderDao).selectById(10);
    }

    @Test
    void getCheckShouldThrowWhenNotFound() {
        when(orderDao.selectById(10)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> orderApi.getCheck(10));
        assertTrue(ex.getMessage().contains(ORDER_NOT_FOUND.value()));
        assertTrue(ex.getMessage().contains("10"));
        verify(orderDao).selectById(10);
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
    void generateInvoiceShouldSetInvoicePathAndStatusInvoiced() throws ApiException {
        Order o = new Order();
        o.setId(1);
        o.setStatus(OrderStatus.CREATED);

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
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();
        List<Order> expected = List.of(new Order());

        when(orderDao.search(1, start, end, OrderStatus.CREATED, 0, 10)).thenReturn(expected);

        List<Order> out = orderApi.search(1, start, end, OrderStatus.CREATED, 0, 10);

        assertSame(expected, out);
        verify(orderDao).search(1, start, end, OrderStatus.CREATED, 0, 10);
    }

    @Test
    void getCountShouldCallDaoGetCount() {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);
        ZonedDateTime end = ZonedDateTime.now();

        when(orderDao.getCount(1, start, end, OrderStatus.INVOICED)).thenReturn(123L);

        Long out = orderApi.getCount(1, start, end, OrderStatus.INVOICED);

        assertEquals(123L, out);
        verify(orderDao).getCount(1, start, end, OrderStatus.INVOICED);
    }

    @Test
    void getItemsByOrderIdShouldCallDao() throws ApiException {
        Order o = new Order();
        o.setId(5);
        when(orderDao.selectById(5)).thenReturn(o);

        List<OrderItem> expected = List.of(new OrderItem());
        when(orderItemDao.selectByOrderId(5)).thenReturn(expected);

        List<OrderItem> out = orderApi.getItemsByOrderId(5);

        assertSame(expected, out);
        verify(orderDao).selectById(5);
        verify(orderItemDao).selectByOrderId(5);
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
    }

    @Test
    void getCheckItemsByOrderIdShouldReturnItemsWhenFound() throws ApiException {
        List<OrderItem> expected = List.of(new OrderItem(), new OrderItem());
        when(orderItemDao.selectByOrderId(9)).thenReturn(expected);

        List<OrderItem> out = orderApi.getCheckItemsByOrderId(9);

        assertSame(expected, out);
        verify(orderItemDao).selectByOrderId(9);
    }

    @Test
    void getItemsByOrderIdsShouldCallDao() {
        List<OrderItem> expected = List.of(new OrderItem());
        when(orderItemDao.selectByOrderIds(List.of(1, 2))).thenReturn(expected);

        List<OrderItem> out = orderApi.getItemsByOrderIds(List.of(1, 2));

        assertSame(expected, out);
        verify(orderItemDao).selectByOrderIds(List.of(1, 2));
    }
}