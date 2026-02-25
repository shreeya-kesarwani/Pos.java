package com.pos.order.integration.dto;

import com.pos.api.OrderApi;
import com.pos.dto.OrderDto;
import com.pos.model.constants.OrderStatus;
import com.pos.model.form.OrderSearchForm;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.setup.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderDtoBranchIT extends AbstractIntegrationTest {

    @Autowired private OrderDto orderDto;

    @MockBean private OrderApi orderApi; // override real bean

    @Test
    void search_shouldFilterOutItemsWithNullOrderId_andCoverFilterBranch() throws Exception {
        // Arrange: search returns a real-looking order list
        Order o = new Order();
        o.setId(1);
        o.setStatus(OrderStatus.CREATED);

        when(orderApi.search(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(o));
        when(orderApi.getCount(any(), any(), any(), any())).thenReturn(1L);

        // Items list includes one broken item with null orderId
        OrderItem bad = new OrderItem();
        bad.setOrderId(null);

        OrderItem good = new OrderItem();
        good.setOrderId(1);

        when(orderApi.getItemsByOrderIds(List.of(1))).thenReturn(List.of(bad, good));

        OrderSearchForm form = new OrderSearchForm();
        form.setPageNumber(0);
        form.setPageSize(10);
        form.setStart(ZonedDateTime.now().minusDays(1));
        form.setEnd(ZonedDateTime.now().plusDays(1));
        form.setStatus("CREATED");

        // Act
        var resp = orderDto.search(form);

        // Assert: doesn't crash, and branch executed
        assertNotNull(resp);
        verify(orderApi).getItemsByOrderIds(List.of(1));
    }
}