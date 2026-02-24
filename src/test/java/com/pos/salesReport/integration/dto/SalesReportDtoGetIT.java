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

class SalesReportDtoGetIT extends AbstractIntegrationTest {

    @Autowired private SalesReportDto salesReportDto;
    @Autowired private TestFactory factory;

    @Test
    void shouldGetSalesReport_happyFlow() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        var product = factory.createProduct("b1", "P1", client.getId(), 100.0, null);
        factory.createInventory(product.getId(), 50);

        // Sales report counts INVOICED orders
        var order = factory.createOrder(OrderStatus.INVOICED, null);
        factory.createOrderItems(order.getId(),
                List.of(TestEntities.orderItem(order.getId(), product.getId(), 2, 10.0))
        );
        flushAndClear();

        SalesReportForm form = new SalesReportForm();
        form.setClientId(client.getId());
        form.setStartDate(LocalDate.now().minusDays(10));
        form.setEndDate(LocalDate.now().plusDays(1));

        var data = salesReportDto.getCheck(form);

        assertNotNull(data);
        assertFalse(data.isEmpty());
    }

    @Test
    void shouldThrowWhenNoInvoicedSalesExist() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        factory.createProduct("b1", "P1", client.getId(), 100.0, null);
        flushAndClear();

        SalesReportForm form = new SalesReportForm();
        form.setClientId(client.getId());
        form.setStartDate(LocalDate.of(2026, 2, 14));
        form.setEndDate(LocalDate.of(2026, 2, 25));

        assertThrows(ApiException.class, () -> salesReportDto.getCheck(form));
    }

    @Test
    void shouldThrowWithSpecificMessageWhenNoDataFound() throws Exception {
        SalesReportForm form = new SalesReportForm();
        form.setClientId(null);
        form.setStartDate(LocalDate.of(2000, 1, 1));
        form.setEndDate(LocalDate.of(2000, 1, 2));

        ApiException ex = assertThrows(ApiException.class, () -> salesReportDto.getCheck(form));
        assertTrue(ex.getMessage().contains("No sales report data found"));
    }

    @Test
    void shouldGetSalesReportWithNullClientId() throws Exception {
        var client = factory.createClient("Acme2", "a2@acme.com");
        var product = factory.createProduct("b2", "P2", client.getId(), 100.0, null);
        factory.createInventory(product.getId(), 50);

        var order = factory.createOrder(OrderStatus.INVOICED, null);
        factory.createOrderItems(order.getId(),
                List.of(TestEntities.orderItem(order.getId(), product.getId(), 3, 15.0))
        );
        flushAndClear();

        SalesReportForm form = new SalesReportForm();
        form.setClientId(null); // no client filter
        form.setStartDate(LocalDate.now().minusDays(10));
        form.setEndDate(LocalDate.now().plusDays(1));

        var data = salesReportDto.getCheck(form);

        assertNotNull(data);
        assertFalse(data.isEmpty());
        assertNotNull(data.get(0).getBarcode());
        assertNotNull(data.get(0).getProductName());
    }
}