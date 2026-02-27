package com.pos.salesReport.integration.dto;

import com.pos.dto.SalesReportDto;
import com.pos.exception.ApiException;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SalesReportDtoValidationIT extends AbstractSalesReportDtoIT {

    @Autowired private SalesReportDto salesReportDto;

    private Integer clientId;

    @BeforeEach
    void setup() {
        var client = TestEntities.newClient("VClient", "v@acme.com");
        clientDao.insert(client);
        clientId = client.getId();
        flushAndClear();
    }

    @Test
    void shouldThrowWhenStartAfterEnd_includesDatesInMessage() {
        ApiException ex = assertThrows(ApiException.class, () ->
                salesReportDto.getCheck(form(clientId,
                        LocalDate.of(2026, 2, 20),
                        LocalDate.of(2026, 2, 10)))
        );

        assertTrue(ex.getMessage().contains("startDate"));
        assertTrue(ex.getMessage().contains("endDate"));
        assertTrue(ex.getMessage().contains("startDate="));
        assertTrue(ex.getMessage().contains("endDate="));
    }

    @Test
    void shouldThrowWhenStartDateIsNull() {
        ApiException ex = assertThrows(ApiException.class, () ->
                salesReportDto.getCheck(form(clientId, null, LocalDate.now()))
        );
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().toLowerCase().contains("start"));
    }

    @Test
    void shouldThrowWhenEndDateIsNull() {
        ApiException ex = assertThrows(ApiException.class, () ->
                salesReportDto.getCheck(form(clientId, LocalDate.now().minusDays(1), null))
        );
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().toLowerCase().contains("end"));
    }

    @Test
    void shouldNotThrowWhenStartBeforeEnd() throws Exception {
        seedOneInvoicedSale(clientId, "vb1");

        assertDoesNotThrow(() ->
                salesReportDto.getCheck(form(clientId,
                        LocalDate.now().minusDays(10),
                        LocalDate.now().plusDays(1)))
        );
    }
}