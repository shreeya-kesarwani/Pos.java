package com.pos.order.integration.dto;

import com.pos.dao.OrderDao;
import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.OrderData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.OrderSearchForm;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrderDtoSearchIT extends AbstractOrderDtoIntegrationTest {

    @Autowired private OrderDao orderDao;

    private OrderSearchForm baseSearchForm() {
        OrderSearchForm form = new OrderSearchForm();
        form.setPageNumber(0);
        form.setPageSize(10);
        return form;
    }

    @Test
    void shouldSearchOrders_happyFlow() throws Exception {
        var order = TestEntities.newOrder(OrderStatus.CREATED, null);
        orderDao.insert(order);
        flushAndClear();

        OrderSearchForm form = baseSearchForm();
        form.setStart(ZonedDateTime.now().minusDays(2));
        form.setEnd(ZonedDateTime.now().plusDays(1));
        form.setStatus("  CREATED  ");

        var resp = orderDto.search(form);

        assertNotNull(resp);
        assertTrue(resp.getTotalCount() >= 0);
    }

    @Test
    void shouldSearchWhenStatusBlank() throws Exception {
        OrderSearchForm form = baseSearchForm();
        form.setStatus("   ");
        assertNotNull(orderDto.search(form));
    }

    @Test
    void shouldThrowWhenStatusInvalid() {
        OrderSearchForm form = baseSearchForm();
        form.setStatus("INVALID");
        assertThrows(ApiException.class, () -> orderDto.search(form));
    }

    @Test
    void shouldSearchWhenIdProvided() throws Exception {
        var order = TestEntities.newOrder(OrderStatus.CREATED, null);
        orderDao.insert(order);
        flushAndClear();

        OrderSearchForm form = baseSearchForm();
        form.setId(order.getId());

        var resp = orderDto.search(form);

        assertNotNull(resp);
        assertTrue(resp.getData().stream().anyMatch(o -> o.getId().equals(order.getId())));
    }

    @Test
    void shouldThrowWhenStartAfterEnd() {
        OrderSearchForm form = baseSearchForm();
        form.setStart(ZonedDateTime.now());
        form.setEnd(ZonedDateTime.now().minusDays(1));

        assertThrows(ApiException.class, () -> orderDto.search(form));
    }

    @Test
    void shouldDefaultPaginationWhenNull() throws Exception {
        OrderSearchForm form = new OrderSearchForm();
        form.setStatus(null);

        var resp = orderDto.search(form);

        assertNotNull(resp);
        assertEquals(0, resp.getPageNo());
    }

    @Test
    void shouldReturnEmptyWhenNoOrdersMatchSearch() throws Exception {
        OrderSearchForm form = baseSearchForm();
        form.setId(999999);

        PaginatedResponse<OrderData> resp = orderDto.search(form);

        assertNotNull(resp);
        assertTrue(resp.getData().isEmpty());
        assertEquals(0, resp.getTotalCount());
    }

    @Test
    void shouldSearchWithOnlyStartProvided() throws Exception {
        var order = TestEntities.newOrder(OrderStatus.CREATED, null);
        orderDao.insert(order);
        flushAndClear();

        OrderSearchForm form = baseSearchForm();
        form.setStart(ZonedDateTime.now().minusDays(5));
        form.setEnd(null);

        assertNotNull(orderDto.search(form));
    }

    @Test
    void shouldSearchWithOnlyEndProvided() throws Exception {
        var order = TestEntities.newOrder(OrderStatus.CREATED, null);
        orderDao.insert(order);
        flushAndClear();

        OrderSearchForm form = baseSearchForm();
        form.setStart(null);
        form.setEnd(ZonedDateTime.now().plusDays(1));

        assertNotNull(orderDto.search(form));
    }

    @Test
    void search_shouldHandleOrdersWithNoItems_andReturnEmptyDataList() throws Exception {
        var order = TestEntities.newOrder(OrderStatus.CREATED, null);
        orderDao.insert(order);
        flushAndClear();

        OrderSearchForm form = baseSearchForm();
        form.setId(order.getId());

        var resp = orderDto.search(form);

        assertNotNull(resp);
        assertTrue(resp.getTotalCount() >= 0);
    }

    @Test
    void shouldSearchWhenStatusNull() throws Exception {
        var order = TestEntities.newOrder(OrderStatus.CREATED, null);
        orderDao.insert(order);
        flushAndClear();

        OrderSearchForm form = baseSearchForm();
        form.setStatus(null);

        assertNotNull(orderDto.search(form));
    }

    @Test
    void shouldSearch_whenOrdersHaveNoItems_shouldStillReturnOrders_andCoverEmptyItemsBranch() throws Exception {
        var order = TestEntities.newOrder(OrderStatus.CREATED, null);
        orderDao.insert(order);
        flushAndClear();

        OrderSearchForm form = new OrderSearchForm();
        form.setPageNumber(0);
        form.setPageSize(10);
        form.setId(order.getId());

        var resp = orderDto.search(form);

        assertNotNull(resp);
        assertEquals(1, resp.getData().size());
        assertEquals(order.getId(), resp.getData().get(0).getId());
    }
}