package com.pos.order.integration.dto;

import com.pos.dto.OrderDto;
import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.OrderData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.OrderForm;
import com.pos.model.form.OrderItemForm;
import com.pos.model.form.OrderSearchForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDtoSearchIT extends AbstractIntegrationTest {

    @Autowired private OrderDto orderDto;
    @Autowired private TestFactory factory;

    @Test
    void shouldSearchOrders_happyFlow() throws Exception {
        // create some data so search isn't totally empty
        factory.createOrder(OrderStatus.CREATED, null);
        flushAndClear();

        OrderSearchForm form = new OrderSearchForm();
        form.setStart(ZonedDateTime.now().minusDays(2));
        form.setEnd(ZonedDateTime.now().plusDays(1));
        form.setStatus("  CREATED  "); // trim branch
        form.setPageNumber(0);
        form.setPageSize(10);

        PaginatedResponse<?> resp = orderDto.search(form);

        assertNotNull(resp);
        assertTrue(resp.getTotalCount() >= 0);
    }

    @Test
    void shouldSearchWhenStatusBlank() throws Exception {
        OrderSearchForm form = new OrderSearchForm();
        form.setStatus("   "); // blank -> treated as null in dto
        form.setPageNumber(0);
        form.setPageSize(10);

        assertNotNull(orderDto.search(form));
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
        form.setPageNumber(0);
        form.setPageSize(10);

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

        assertNotNull(resp);
        assertTrue(resp.getData().stream().anyMatch(o -> o.getId().equals(order.getId())));
    }

    @Test
    void shouldThrowWhenStartAfterEnd() {
        OrderSearchForm form = new OrderSearchForm();
        form.setStart(ZonedDateTime.now());
        form.setEnd(ZonedDateTime.now().minusDays(1));
        form.setPageNumber(0);
        form.setPageSize(10);

        assertThrows(ApiException.class, () -> orderDto.search(form));
    }

    @Test
    void shouldDefaultPaginationWhenNull() throws Exception {
        OrderSearchForm form = new OrderSearchForm();
        form.setStatus(null);
        form.setPageNumber(null); // default branch
        form.setPageSize(null);   // default branch

        var resp = orderDto.search(form);

        assertNotNull(resp);
        assertEquals(0, resp.getPageNo()); // proves default pageNumber applied
        assertNotNull(resp.getData());
    }

    @Test
    void shouldReturnEmptyWhenNoOrdersMatchSearch() throws Exception {
        OrderSearchForm form = new OrderSearchForm();
        form.setId(999999); // non-existent ID
        form.setPageNumber(0);
        form.setPageSize(10);

        PaginatedResponse<OrderData> resp = orderDto.search(form);

        assertNotNull(resp);
        assertTrue(resp.getData().isEmpty());
        assertEquals(0, resp.getTotalCount());
    }

    @Test
    void shouldSearchWithStartDateOnlyNoEndDate() throws Exception {
        factory.createOrder(OrderStatus.CREATED, null);
        flushAndClear();

        OrderSearchForm form = new OrderSearchForm();
        form.setStart(ZonedDateTime.now().minusDays(5));
        form.setEnd(null); // only start date → skips date range validation
        form.setPageNumber(0);
        form.setPageSize(10);

        var resp = orderDto.search(form);
        assertNotNull(resp);
        assertNotNull(resp.getData());
    }

    @Test
    void shouldSearchWithEndDateOnlyNoStartDate() throws Exception {
        factory.createOrder(OrderStatus.CREATED, null);
        flushAndClear();

        OrderSearchForm form = new OrderSearchForm();
        form.setStart(null); // only end date → skips date range validation
        form.setEnd(ZonedDateTime.now().plusDays(1));
        form.setPageNumber(0);
        form.setPageSize(10);

        var resp = orderDto.search(form);
        assertNotNull(resp);
        assertNotNull(resp.getData());
    }

    @Test
    void shouldSearchOrdersWithItemsAndReturnOrderData() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        var product = factory.createProduct("b1", "P1", client.getId(), 100.0, null);
        factory.createInventory(product.getId(), 50);
        flushAndClear();

        OrderItemForm item = new OrderItemForm();
        item.setBarcode("b1");
        item.setQuantity(2);
        item.setSellingPrice(10.0);

        OrderForm orderForm = new OrderForm();
        orderForm.setItems(List.of(item));

        Integer orderId = orderDto.create(orderForm);
        flushAndClear();

        OrderSearchForm searchForm = new OrderSearchForm();
        searchForm.setId(orderId);
        searchForm.setPageNumber(0);
        searchForm.setPageSize(10);

        var resp = orderDto.search(searchForm);

        assertNotNull(resp);
        assertFalse(resp.getData().isEmpty());
        assertEquals(orderId, resp.getData().get(0).getId());
    }

    @Test
    void shouldSearchWithValidDateRangeAndStatus() throws Exception {
        factory.createOrder(OrderStatus.INVOICED, null);
        flushAndClear();

        OrderSearchForm form = new OrderSearchForm();
        form.setStart(ZonedDateTime.now().minusDays(1));
        form.setEnd(ZonedDateTime.now().plusDays(1));
        form.setStatus("INVOICED");
        form.setPageNumber(0);
        form.setPageSize(5);

        var resp = orderDto.search(form);

        assertNotNull(resp);
        assertNotNull(resp.getData());
    }

    @Test
    void shouldReturnOrderEvenWhenItHasNoItems() throws Exception {
        var order = factory.createOrder(OrderStatus.CREATED, null);
        flushAndClear();

        OrderSearchForm form = new OrderSearchForm();
        form.setId(order.getId());     // ensures order list is non-empty
        form.setPageNumber(0);
        form.setPageSize(10);

        var resp = orderDto.search(form);

        assertNotNull(resp);
        assertFalse(resp.getData().isEmpty());
        assertEquals(order.getId(), resp.getData().getFirst().getId());
    }

    @Test
    void search_whenStartAndEndNull_shouldNotThrow() throws Exception {
        OrderSearchForm form = new OrderSearchForm();
        form.setStart(null);
        form.setEnd(null);
        form.setPageNumber(0);
        form.setPageSize(10);

        var resp = orderDto.search(form);

        assertNotNull(resp);
    }

    @Test
    void search_whenOnlyStartProvided_shouldNotThrow() throws Exception {
        OrderSearchForm form = new OrderSearchForm();
        form.setStart(ZonedDateTime.now().minusDays(1));
        form.setEnd(null);
        form.setPageNumber(0);
        form.setPageSize(10);

        var resp = orderDto.search(form);

        assertNotNull(resp);
    }

    @Test
    void search_whenOnlyEndProvided_shouldNotThrow() throws Exception {
        OrderSearchForm form = new OrderSearchForm();
        form.setStart(null);
        form.setEnd(ZonedDateTime.now());
        form.setPageNumber(0);
        form.setPageSize(10);

        var resp = orderDto.search(form);

        assertNotNull(resp);
    }
}