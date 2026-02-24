package com.pos.daySales.integration.dto;

import com.pos.dto.DaySalesDto;
import com.pos.model.form.DaySalesForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DaySalesDtoGetIT extends AbstractIntegrationTest {

    @Autowired DaySalesDto daySalesDto;
    @Autowired TestFactory factory;

    private DaySalesForm formWithStart(ZonedDateTime start) {
        DaySalesForm form = new DaySalesForm();
        form.setStartDate(start);
        return form;
    }

    @Test
    void shouldReturnDaySales_happyFlow() throws Exception {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);

        factory.createDaySales(start, 5, 12, 1000.0);
        flushAndClear();

        var out = daySalesDto.get(formWithStart(start));

        assertNotNull(out);
        assertFalse(out.isEmpty());

        // Stronger check: at least one row exists for that date (if DaySalesData exposes date)
        // If DaySalesData has getDate()/getOrders()/getItems()/getRevenue(), assert them like:
        // assertTrue(out.stream().anyMatch(d -> start.toLocalDate().equals(d.getDate())));
    }
}