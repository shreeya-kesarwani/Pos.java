package com.pos.salesReport.integration.dto;

import com.pos.dto.SalesReportDto;
import com.pos.model.constants.OrderStatus;
import com.pos.model.form.SalesReportForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestEntities;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SalesReportDtoGetIT extends AbstractIntegrationTest {

    @Autowired SalesReportDto salesReportDto;
    @Autowired TestFactory factory;

    @Test
    void shouldGetSalesReport_happyFlow() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        var product = factory.createProduct("b1", "P1", client.getId(), 100.0, null);
        factory.createInventory(product.getId(), 50);

        // IMPORTANT: Sales report usually counts only INVOICED orders
        var order = factory.createOrder(OrderStatus.INVOICED, null);
        factory.createOrderItems(order.getId(),
                java.util.List.of(TestEntities.orderItem(order.getId(), product.getId(), 2, 10.0))
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
}