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

    @Mock private OrderDao orderDao;
    @InjectMocks private OrderApi orderApi;

    @Test
    void getCheck_shouldThrow_whenOrderMissing() {
        when(orderDao.select(123)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> orderApi.getCheck(123));
        assertTrue(ex.getMessage().contains("Order not found"));

        verify(orderDao).select(123);
        verifyNoMoreInteractions(orderDao);
    }

    @Test
    void updateStatus_shouldUpdate_whenOrderExists() throws ApiException {
        Order order = new Order();
        order.setId(5);
        order.setStatus(OrderStatus.CREATED);

        when(orderDao.select(5)).thenReturn(order);

        orderApi.updateStatus(5, OrderStatus.INVOICED);

        assertEquals(OrderStatus.INVOICED, order.getStatus());
        verify(orderDao).select(5);
        verify(orderDao).update(order);
        verifyNoMoreInteractions(orderDao);
    }
}
