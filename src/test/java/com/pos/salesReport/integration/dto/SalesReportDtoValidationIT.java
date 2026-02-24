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

    @Autowired SalesReportDto salesReportDto;
    @Autowired private TestFactory factory;

    @Test
    void shouldThrowWhenStartAfterEnd() {
        SalesReportForm form = new SalesReportForm();
        form.setClientId(7);
        form.setStartDate(LocalDate.of(2026, 2, 3));
        form.setEndDate(LocalDate.of(2026, 2, 2));

        ApiException ex = assertThrows(ApiException.class, () -> salesReportDto.getCheck(form));
        assertNotNull(ex.getMessage());
    }

    @Test
    void shouldThrowWhenNoSalesReportDataFound() {
        SalesReportForm form = new SalesReportForm();
        form.setStartDate(LocalDate.now().minusDays(1));
        form.setEndDate(LocalDate.now());
        form.setClientId(999999);

        assertThrows(ApiException.class, () -> salesReportDto.getCheck(form));
    }

    @Test
    void shouldWorkWhenClientIdIsNull() throws Exception {
        // seed 1 invoiced order for some client
        var client = factory.createClient("Acme", "a@acme.com");
        var product = factory.createProduct("b1", "P1", client.getId(), 100.0, null);
        factory.createInventory(product.getId(), 10);
        var order = factory.createOrder(OrderStatus.INVOICED, null);
        factory.createOrderItems(order.getId(),
                List.of(TestEntities.orderItem(order.getId(), product.getId(), 1, 10.0)));
        flushAndClear();

        SalesReportForm form = new SalesReportForm();
        form.setStartDate(LocalDate.now().minusDays(10));
        form.setEndDate(LocalDate.now().plusDays(1));
        form.setClientId(null); // branch

        var out = salesReportDto.getCheck(form);
        assertNotNull(out);
        assertFalse(out.isEmpty());
    }
}