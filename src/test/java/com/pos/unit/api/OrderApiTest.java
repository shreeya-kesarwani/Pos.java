package com.pos.unit.api;

import com.pos.api.OrderApi;
import com.pos.dao.OrderDao;
import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.pojo.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApiTest {

    @Mock
    private OrderDao orderDao;

    @InjectMocks
    private OrderApi orderApi;

    // ---------- getCheck ----------

    @Test
    void getCheck_shouldThrow_whenOrderMissing() {
        when(orderDao.select(123, Order.class)).thenReturn(null);

        ApiException ex = assertThrows(
                ApiException.class,
                () -> orderApi.getCheck(123)
        );

        assertTrue(ex.getMessage().contains("Order not found"));
        verify(orderDao).select(123, Order.class);
        verifyNoMoreInteractions(orderDao);
    }

    @Test
    void getCheck_shouldReturnOrder_whenFound() throws ApiException {
        Order order = new Order();
        order.setId(123);

        when(orderDao.select(123, Order.class)).thenReturn(order);

        Order result = orderApi.getCheck(123);

        assertEquals(order, result);
        verify(orderDao).select(123, Order.class);
        verifyNoMoreInteractions(orderDao);
    }

    // ---------- updateStatus ----------

    @Test
    void updateStatus_shouldFail_whenOrderAlreadyInvoiced() {
        Order order = new Order();
        order.setStatus(OrderStatus.INVOICED);

        when(orderDao.select(1, Order.class)).thenReturn(order);

        ApiException ex = assertThrows(
                ApiException.class,
                () -> orderApi.updateStatus(1, OrderStatus.CREATED)
        );

        assertTrue(ex.getMessage().contains("INVOICED"));
        verify(orderDao).select(1, Order.class);
        verifyNoMoreInteractions(orderDao); // ✅ no update call
    }

    @Test
    void updateStatus_shouldUpdateStatus_whenValid() throws ApiException {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);

        when(orderDao.select(1, Order.class)).thenReturn(order);

        orderApi.updateStatus(1, OrderStatus.CREATED);

        assertEquals(OrderStatus.CREATED, order.getStatus());
        verify(orderDao).select(1, Order.class);
        verifyNoMoreInteractions(orderDao);
    }

    // ---------- attachInvoice ----------

    @Test
    void attachInvoice_shouldFail_whenOrderNotCreated() {
        Order order = new Order();
        order.setStatus(OrderStatus.INVOICED);

        when(orderDao.select(1, Order.class)).thenReturn(order);

        ApiException ex = assertThrows(
                ApiException.class,
                () -> orderApi.attachInvoice(1, "/tmp/inv.pdf")
        );

        assertTrue(ex.getMessage().contains("already invoiced"));
        verify(orderDao).select(1, Order.class);
        verifyNoMoreInteractions(orderDao);
    }

    @Test
    void attachInvoice_shouldMarkOrderInvoiced() throws ApiException {
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);

        when(orderDao.select(1, Order.class)).thenReturn(order);

        orderApi.attachInvoice(1, "/tmp/inv.pdf");

        assertEquals(OrderStatus.INVOICED, order.getStatus());
        assertEquals("/tmp/inv.pdf", order.getInvoicePath());
        verify(orderDao).select(1, Order.class);
        verifyNoMoreInteractions(orderDao);
    }
}
