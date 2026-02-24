package com.pos.salesReport.integration.dto;

import com.pos.dto.SalesReportDto;
import com.pos.exception.ApiException;
import com.pos.model.constants.OrderStatus;
import com.pos.model.form.SalesReportForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestEntities;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SalesReportDtoValidationIT extends AbstractIntegrationTest {

    @Autowired private SalesReportDto salesReportDto;
    @Autowired private TestFactory factory;

    @Test
    void shouldThrowWhenStartAfterEnd() {
        var client = factory.createClient("Acme3", "a3@acme.com");

        SalesReportForm form = new SalesReportForm();
        form.setClientId(client.getId());
        form.setStartDate(LocalDate.of(2026, 2, 3));
        form.setEndDate(LocalDate.of(2026, 2, 2));

        assertThrows(ApiException.class, () -> salesReportDto.getCheck(form));
    }

    @Test
    void shouldWorkWhenClientIdIsNull() throws Exception {
        // seed 1 invoiced order for some client
        var client = factory.createClient("Acme4", "a4@acme.com");
        var product = factory.createProduct("b4", "P4", client.getId(), 100.0, null);
        factory.createInventory(product.getId(), 10);

        var order = factory.createOrder(OrderStatus.INVOICED, null);
        factory.createOrderItems(order.getId(),
                List.of(TestEntities.orderItem(order.getId(), product.getId(), 1, 10.0))
        );
        flushAndClear();

        SalesReportForm form = new SalesReportForm();
        form.setStartDate(LocalDate.now().minusDays(10));
        form.setEndDate(LocalDate.now().plusDays(1));
        form.setClientId(null); // client filter disabled

        var out = salesReportDto.getCheck(form);

        assertNotNull(out);
        assertFalse(out.isEmpty());
    }

    @Test
    void shouldThrowWhenStartDateIsNull() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");

        SalesReportForm form = new SalesReportForm();
        form.setClientId(client.getId());
        form.setStartDate(null);
        form.setEndDate(LocalDate.now());

        ApiException ex = assertThrows(ApiException.class, () -> salesReportDto.getCheck(form));
        assertTrue(ex.getMessage().toLowerCase().contains("startdate"));
    }

    @Test
    void shouldThrowWhenEndDateIsNull() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");

        SalesReportForm form = new SalesReportForm();
        form.setClientId(client.getId());
        form.setStartDate(LocalDate.now().minusDays(2));
        form.setEndDate(null);

        ApiException ex = assertThrows(ApiException.class, () -> salesReportDto.getCheck(form));
        assertTrue(ex.getMessage().toLowerCase().contains("enddate"));
    }

    @Test
    void shouldWorkWithEqualStartAndEndDates() throws Exception {
        var client = factory.createClient("Acme5", "a5@acme.com");
        var product = factory.createProduct("b5", "P5", client.getId(), 100.0, null);
        factory.createInventory(product.getId(), 10);

        var order = factory.createOrder(OrderStatus.INVOICED, null);
        factory.createOrderItems(order.getId(),
                List.of(TestEntities.orderItem(order.getId(), product.getId(), 1, 10.0))
        );
        flushAndClear();

        SalesReportForm form = new SalesReportForm();
        form.setStartDate(LocalDate.now());
        form.setEndDate(LocalDate.now()); // same as start → start.isAfter(end) is false
        form.setClientId(client.getId());

        var out = salesReportDto.getCheck(form);
        assertNotNull(out);
        assertFalse(out.isEmpty());
    }

    @Test
    void shouldThrowWithStartAfterEndMessage() {
        SalesReportForm form = new SalesReportForm();
        form.setStartDate(LocalDate.of(2026, 2, 20));
        form.setEndDate(LocalDate.of(2026, 2, 10));

        ApiException ex = assertThrows(ApiException.class, () -> salesReportDto.getCheck(form));
        assertTrue(ex.getMessage().contains("startDate cannot be after endDate"));
    }
}