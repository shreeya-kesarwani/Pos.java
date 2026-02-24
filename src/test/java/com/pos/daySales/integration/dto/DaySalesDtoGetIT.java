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

    @Test
    void shouldReturnDaySales_happyFlow() throws Exception {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);

        // Seed one row so API/DAO can return it
        factory.createDaySales(start, 5, 12, 1000.0);
        flushAndClear();

        DaySalesForm form = new DaySalesForm();
        form.setStartDate(start);

        var out = daySalesDto.get(form);

        assertNotNull(out);
        assertTrue(out.size() >= 1);
        // if DaySalesData has fields like revenue/orders/items/date, assert them once you check the DTO mapping
    }
}