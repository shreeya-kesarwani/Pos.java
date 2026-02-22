package com.pos.daySales.unit;

import com.pos.api.DaySalesApi;
import com.pos.dao.DaySalesDao;
import com.pos.exception.ApiException;
import com.pos.pojo.DaySales;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static com.pos.model.constants.ErrorMessages.DATE_REQUIRED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DaySalesApiTest {

    @InjectMocks
    private DaySalesApi daySalesApi;

    @Mock
    private DaySalesDao daySalesDao;

    @Captor
    private ArgumentCaptor<ZonedDateTime> zdtCaptor;

    @Captor
    private ArgumentCaptor<DaySales> daySalesCaptor;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Test
    void getDaySalesShouldThrowWhenDateNull() {
        ApiException ex = assertThrows(ApiException.class, () -> daySalesApi.getDaySales(null));
        assertEquals(DATE_REQUIRED.value(), ex.getMessage());
        verifyNoInteractions(daySalesDao);
    }

    @Test
    void getDaySalesShouldReturnEmptyListWhenDaoReturnsNull() throws ApiException {
        ZonedDateTime input = ZonedDateTime.of(2026, 2, 15, 23, 30, 0, 0, ZoneId.of("UTC"));
        when(daySalesDao.selectInRange(any(), any())).thenReturn(null);

        List<DaySales> out = daySalesApi.getDaySales(input);

        assertNotNull(out);
        assertTrue(out.isEmpty());

        // ✅ DAO expects 2 params
        verify(daySalesDao).selectInRange(zdtCaptor.capture(), zdtCaptor.capture());
        ZonedDateTime start = zdtCaptor.getAllValues().get(0);
        ZonedDateTime end = zdtCaptor.getAllValues().get(1);

        assertEquals(IST, start.getZone());
        assertEquals(IST, end.getZone());
        assertEquals(start.toLocalDate().atStartOfDay(IST), start);
        assertEquals(start.plusDays(1), end);
    }

    @Test
    void getDaySalesShouldReturnRowsWhenDaoReturnsList() throws ApiException {
        ZonedDateTime input = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));
        List<DaySales> rows = List.of(new DaySales(), new DaySales());
        when(daySalesDao.selectInRange(any(), any())).thenReturn(rows);

        List<DaySales> out = daySalesApi.getDaySales(input);

        assertSame(rows, out);
        verify(daySalesDao).selectInRange(any(), any());
        verify(daySalesDao, never()).insert(any());
    }

    @Test
    void calculateDaySalesShouldAggregateAndInsertPojo() throws ApiException {
        Object[] aggRow = new Object[]{2L, 5L, 250.75};
        when(daySalesDao.selectInvoicedSalesAggregatesForDay(any(), any())).thenReturn(aggRow);

        daySalesApi.calculateDaySales();

        verify(daySalesDao).selectInvoicedSalesAggregatesForDay(zdtCaptor.capture(), zdtCaptor.capture());
        ZonedDateTime start = zdtCaptor.getAllValues().get(0);
        ZonedDateTime end = zdtCaptor.getAllValues().get(1);

        assertEquals(IST, start.getZone());
        assertEquals(IST, end.getZone());
        assertEquals(start.toLocalDate().atStartOfDay(IST), start);
        assertEquals(start.plusDays(1), end);

        verify(daySalesDao).insert(daySalesCaptor.capture());
        DaySales inserted = daySalesCaptor.getValue();

        assertNotNull(inserted);
        assertEquals(start, inserted.getDate());
        assertEquals(2, inserted.getInvoicedOrdersCount());
        assertEquals(5, inserted.getInvoicedItemsCount());
        assertEquals(250.75, inserted.getTotalRevenue());
    }

    @Test
    void calculateDaySalesShouldStillInsertWhenDaoReturnsNullsInRow() throws ApiException {
        Object[] aggRow = new Object[]{null, null, null};
        when(daySalesDao.selectInvoicedSalesAggregatesForDay(any(), any())).thenReturn(aggRow);

        daySalesApi.calculateDaySales();

        verify(daySalesDao).insert(any(DaySales.class));
    }
}