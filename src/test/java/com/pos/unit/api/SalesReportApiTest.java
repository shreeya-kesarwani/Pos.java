package com.pos.unit.api;

import com.pos.api.SalesReportApi;
import com.pos.dao.SalesReportDao;
import com.pos.exception.ApiException;
import com.pos.model.data.SalesReportData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.List;

import static com.pos.model.constants.ErrorMessages.SALES_REPORT_EMPTY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SalesReportApiTest {

    @InjectMocks
    private SalesReportApi salesReportApi;

    @Mock
    private SalesReportDao salesReportDao;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getSalesReport_shouldDelegateToDao() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 10);
        Integer clientId = 123;

        List<SalesReportData> expected = List.of(new SalesReportData(), new SalesReportData());
        when(salesReportDao.getSalesReportRows(start, end, clientId)).thenReturn(expected);

        List<SalesReportData> out = salesReportApi.getSalesReport(start, end, clientId);

        assertSame(expected, out);
        verify(salesReportDao, times(1)).getSalesReportRows(start, end, clientId);
        verifyNoMoreInteractions(salesReportDao);
    }

    @Test
    void getCheckSalesReport_shouldReturnRows_whenNotEmpty() throws Exception {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 10);
        Integer clientId = null;

        List<SalesReportData> rows = List.of(new SalesReportData());
        when(salesReportDao.getSalesReportRows(start, end, clientId)).thenReturn(rows);

        List<SalesReportData> out = salesReportApi.getCheckSalesReport(start, end, clientId);

        assertSame(rows, out);
        verify(salesReportDao, times(1)).getSalesReportRows(start, end, clientId);
    }

    @Test
    void getCheckSalesReport_shouldThrow_whenDaoReturnsNull() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 10);
        Integer clientId = 7;

        when(salesReportDao.getSalesReportRows(start, end, clientId)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class,
                () -> salesReportApi.getCheckSalesReport(start, end, clientId));

        assertTrue(ex.getMessage().startsWith(SALES_REPORT_EMPTY.value()));
        assertTrue(ex.getMessage().contains("startDate=" + start));
        assertTrue(ex.getMessage().contains("endDate=" + end));
        assertTrue(ex.getMessage().contains("clientId=" + clientId));

        verify(salesReportDao, times(1)).getSalesReportRows(start, end, clientId);
    }

    @Test
    void getCheckSalesReport_shouldThrow_whenDaoReturnsEmptyList() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 10);
        Integer clientId = 7;

        when(salesReportDao.getSalesReportRows(start, end, clientId)).thenReturn(List.of());

        ApiException ex = assertThrows(ApiException.class,
                () -> salesReportApi.getCheckSalesReport(start, end, clientId));

        assertTrue(ex.getMessage().startsWith(SALES_REPORT_EMPTY.value()));
        assertTrue(ex.getMessage().contains("startDate=" + start));
        assertTrue(ex.getMessage().contains("endDate=" + end));
        assertTrue(ex.getMessage().contains("clientId=" + clientId));

        verify(salesReportDao, times(1)).getSalesReportRows(start, end, clientId);
    }
}
