package com.pos.daySales.integration.dto;

import com.pos.dao.DaySalesDao;
import com.pos.dto.DaySalesDto;
import com.pos.model.form.DaySalesForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DaySalesDtoGetIT extends AbstractIntegrationTest {

    @Autowired DaySalesDto daySalesDto;
    @Autowired DaySalesDao daySalesDao;

    private DaySalesForm formWithStart(ZonedDateTime start) {
        DaySalesForm form = new DaySalesForm();
        form.setStartDate(start);
        return form;
    }

    @Test
    void shouldReturnDaySales_happyFlow() throws Exception {
        ZonedDateTime start = ZonedDateTime.now().minusDays(1);

        daySalesDao.insert(TestEntities.newDaySales(start, 5, 12, 1000.0));
        flushAndClear();

        var out = daySalesDto.get(formWithStart(start));

        assertNotNull(out);
        assertFalse(out.isEmpty());
    }
}