package com.pos.order.integration.dto;

import com.pos.dto.OrderDto;
import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.model.form.OrderSearchForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrderDtoSearchIT extends AbstractIntegrationTest {

    @Autowired OrderDto orderDto;
    @Autowired private TestFactory factory;

    @Test
    void shouldSearchOrders_happyFlow() throws Exception {
        OrderSearchForm form = new OrderSearchForm();
        form.setStart(ZonedDateTime.now().minusDays(2));
        form.setEnd(ZonedDateTime.now().plusDays(1));
        form.setStatus("  CREATED  ");
        form.setPageNumber(0);
        form.setPageSize(10);

        var resp = orderDto.search(form);
        assertNotNull(resp);
        assertTrue(resp.getTotalCount() >= 0);
    }

    @Test
    void shouldSearchWhenStatusBlank() throws Exception {
        OrderSearchForm form = new OrderSearchForm();
        form.setStatus("   ");
        form.setPageNumber(0);
        form.setPageSize(10);

        var resp = orderDto.search(form);

        assertNotNull(resp);
    }


    @Test
    void shouldDefaultPaginationWhenNull() throws Exception {
        OrderSearchForm form = new OrderSearchForm();
        form.setStatus(null);
        form.setPageNumber(null);
        form.setPageSize(null);

        var resp = orderDto.search(form);

        assertNotNull(resp);
    }

    @Test
    void shouldSearchWhenStatusNull() throws Exception {
        OrderSearchForm form = new OrderSearchForm();
        form.setStatus(null);
        form.setPageNumber(0);
        form.setPageSize(10);

        assertNotNull(orderDto.search(form));
    }

    @Test
    void shouldThrowWhenStatusInvalid() {
        OrderSearchForm form = new OrderSearchForm();
        form.setStatus("INVALID");

        assertThrows(ApiException.class, () -> orderDto.search(form));
    }

    @Test
    void shouldSearchWhenIdProvided() throws Exception {
        var order = factory.createOrder(OrderStatus.CREATED, null);
        flushAndClear();

        OrderSearchForm form = new OrderSearchForm();
        form.setId(order.getId());
        form.setPageNumber(0);
        form.setPageSize(10);

        var resp = orderDto.search(form);
        assertTrue(resp.getData().stream().anyMatch(o -> o.getId().equals(order.getId())));
    }
}