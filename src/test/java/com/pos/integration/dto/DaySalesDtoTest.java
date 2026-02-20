package com.pos.integration.dto;

import com.pos.api.DaySalesApi;
import com.pos.dto.DaySalesDto;
import com.pos.exception.ApiException;
import com.pos.model.data.DaySalesData;
import com.pos.model.form.DaySalesForm;
import com.pos.pojo.DaySales;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DaySalesDtoTest {

    @Mock private DaySalesApi daySalesApi;
    @Mock private Validator validator;

    @InjectMocks private DaySalesDto daySalesDto;

    @Test
    void getThrowsWhenStartDateMissing() {
        DaySalesForm form = new DaySalesForm();

        when(validator.validate(any(DaySalesForm.class))).thenReturn(Set.of());

        ApiException ex = assertThrows(ApiException.class, () -> daySalesDto.get(form));
        assertNotNull(ex.getMessage());

        verifyNoInteractions(daySalesApi);
    }

    @Test
    void getCallsApiAndConverts() throws Exception {
        DaySalesForm form = new DaySalesForm();
        form.setStartDate(ZonedDateTime.now());

        when(validator.validate(any(DaySalesForm.class))).thenReturn(Set.of());

        DaySales row = new DaySales();
        when(daySalesApi.getDaySales(eq(form.getStartDate()))).thenReturn(List.of(row));

        List<DaySalesData> out = daySalesDto.get(form);

        assertEquals(1, out.size());
        verify(daySalesApi).getDaySales(eq(form.getStartDate()));
    }

    @Test
    void getThrowsWhenBeanValidationFails() {
        DaySalesForm form = new DaySalesForm();

        @SuppressWarnings("unchecked")
        ConstraintViolation<DaySalesForm> v = mock(ConstraintViolation.class);
        when(v.getMessage()).thenReturn("invalid");

        when(validator.validate(any(DaySalesForm.class))).thenReturn(Set.of(v));

        ApiException ex = assertThrows(ApiException.class, () -> daySalesDto.get(form));
        assertEquals("invalid", ex.getMessage());
    }
}