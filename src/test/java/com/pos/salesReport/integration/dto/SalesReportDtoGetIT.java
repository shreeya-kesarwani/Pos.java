package com.pos.salesReport.integration.dto;

import com.pos.dto.SalesReportDto;
import com.pos.exception.ApiException;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SalesReportDtoGetIT extends AbstractSalesReportDtoIT {

    @Autowired private SalesReportDto salesReportDto;

    @Test
    void shouldGetSalesReport_happyFlow() throws Exception {
        var client = TestEntities.newClient("Acme", "a@acme.com");
        clientDao.insert(client);

        seedOneInvoicedSale(client.getId(), "b1");

        var data = salesReportDto.getCheck(
                form(client.getId(), LocalDate.now().minusDays(10), LocalDate.now().plusDays(1))
        );

        assertNotNull(data);
        assertFalse(data.isEmpty());
    }

    @Test
    void shouldGetSalesReportWithNullClientId() throws Exception {
        var client = TestEntities.newClient("Acme2", "a2@acme.com");
        clientDao.insert(client);

        seedOneInvoicedSale(client.getId(), "b2");

        var data = salesReportDto.getCheck(
                form(null, LocalDate.now().minusDays(10), LocalDate.now().plusDays(1))
        );

        assertNotNull(data);
        assertFalse(data.isEmpty());
        assertNotNull(data.getFirst().getBarcode());
        assertNotNull(data.getFirst().getProductName());
    }

    @Test
    void shouldThrowWhenNoDataFound() {
        ApiException ex = assertThrows(ApiException.class, () ->
                salesReportDto.getCheck(form(null, LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 2)))
        );
        assertTrue(ex.getMessage().contains("No sales report data found"));
    }
}