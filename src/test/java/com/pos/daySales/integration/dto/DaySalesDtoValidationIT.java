package com.pos.daySales.integration.dto;

import com.pos.dto.DaySalesDto;
import com.pos.exception.ApiException;
import com.pos.model.form.DaySalesForm;
import com.pos.setup.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class DaySalesDtoValidationIT extends AbstractIntegrationTest {

    @Autowired DaySalesDto daySalesDto;

    @Test
    void shouldThrowWhenFormIsNull() {
        assertThrows(IllegalArgumentException.class, () -> daySalesDto.get(null));
    }

    @Test
    void shouldThrowWhenStartDateMissing() {
        DaySalesForm form = new DaySalesForm(); // startDate null

        ApiException ex = assertThrows(ApiException.class, () -> daySalesDto.get(form));

        // Bean Validation message can vary slightly, so keep it robust:
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().toLowerCase().contains("must not be null"));
    }
}