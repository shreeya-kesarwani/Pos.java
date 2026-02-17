package com.pos.integration.dto;

import com.pos.api.SalesReportApi;
import com.pos.dto.SalesReportDto;
import com.pos.exception.ApiException;
import com.pos.model.data.SalesReportData;
import com.pos.model.form.SalesReportForm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesReportDtoTest {

    @Mock private SalesReportApi salesReportApi;
    @Mock private Validator validator;

    @InjectMocks private SalesReportDto salesReportDto;

    @Test
    void getCheck_shouldValidateDatesAndCallApi() throws Exception {
        SalesReportForm form = new SalesReportForm();
        form.setClientId(7);
        form.setStartDate(LocalDate.of(2026, 2, 1));
        form.setEndDate(LocalDate.of(2026, 2, 2));

        when(validator.validate(any(SalesReportForm.class))).thenReturn(Set.of());
        when(salesReportApi.getCheckSalesReport(eq(form.getStartDate()), eq(form.getEndDate()), eq(7)))
                .thenReturn(List.of());

        List<SalesReportData> data = salesReportDto.getCheck(form);
        assertNotNull(data);
        verify(salesReportApi).getCheckSalesReport(eq(form.getStartDate()), eq(form.getEndDate()), eq(7));
    }

    @Test
    void getCheck_shouldThrow_whenStartAfterEnd() {
        SalesReportForm form = new SalesReportForm();
        form.setClientId(7);
        form.setStartDate(LocalDate.of(2026, 2, 3));
        form.setEndDate(LocalDate.of(2026, 2, 2));

        when(validator.validate(any(SalesReportForm.class))).thenReturn(Set.of());

        ApiException ex = assertThrows(ApiException.class, () -> salesReportDto.getCheck(form));
        assertTrue(ex.getMessage().toLowerCase().contains("startdate"));
        verifyNoInteractions(salesReportApi);
    }

    @Test
    void getCheck_shouldThrow_whenBeanValidationFails() {
        SalesReportForm form = new SalesReportForm();

        @SuppressWarnings("unchecked")
        ConstraintViolation<SalesReportForm> v = mock(ConstraintViolation.class);
        when(v.getMessage()).thenReturn("invalid");
        when(validator.validate(any(SalesReportForm.class))).thenReturn(Set.of(v));

        ApiException ex = assertThrows(ApiException.class, () -> salesReportDto.getCheck(form));
        assertEquals("invalid", ex.getMessage());
    }
}
