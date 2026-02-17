package com.pos.unit.api;

import com.pos.api.DaySalesApi;
import com.pos.dao.DaySalesDao;
import com.pos.exception.ApiException;
import com.pos.pojo.DaySales;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static com.pos.model.constants.ErrorMessages.DATE_REQUIRED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getDaySales_shouldThrow_whenDateNull() {
        ApiException ex = assertThrows(ApiException.class, () -> daySalesApi.getDaySales(null));
        assertEquals(DATE_REQUIRED.value(), ex.getMessage());
        verifyNoInteractions(daySalesDao);
    }

    @Test
    void getDaySales_shouldQueryBusinessDayRange_andReturnEmptyListWhenDaoReturnsNull() throws Exception {
        // pick a time in a non-business zone to verify conversion
        ZonedDateTime input = ZonedDateTime.of(2026, 2, 15, 23, 30, 0, 0, ZoneId.of("UTC"));
        when(daySalesDao.selectInRange(any(), any())).thenReturn(null);

        List<DaySales> out = daySalesApi.getDaySales(input);

        assertNotNull(out);
        assertTrue(out.isEmpty());

        verify(daySalesDao).selectInRange(zdtCaptor.capture(), zdtCaptor.capture());
        ZonedDateTime start = zdtCaptor.getAllValues().get(0);
        ZonedDateTime end = zdtCaptor.getAllValues().get(1);

        // Must be in BUSINESS_ZONE
        assertEquals(IST, start.getZone());
        assertEquals(IST, end.getZone());

        // Must be start-of-day in BUSINESS_ZONE and end = start + 1 day
        assertEquals(start.toLocalDate().atStartOfDay(IST), start);
        assertEquals(start.plusDays(1), end);
    }

    @Test
    void getDaySales_shouldReturnRows_whenDaoReturnsList() throws Exception {
        ZonedDateTime input = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));
        List<DaySales> rows = List.of(new DaySales(), new DaySales());
        when(daySalesDao.selectInRange(any(), any())).thenReturn(rows);

        List<DaySales> out = daySalesApi.getDaySales(input);

        assertSame(rows, out);
        verify(daySalesDao, times(1)).selectInRange(any(), any());
        verify(daySalesDao, never()).insert(any());
    }

    @Test
    void calculateDaySales_shouldAggregateAndInsertPojo() throws Exception {
        // row[0]=orders count, row[1]=items count, row[2]=revenue
        Object[] aggRow = new Object[] { 2L, 5L, 250.75 };
        when(daySalesDao.selectInvoicedSalesAggregatesForDay(any(), any())).thenReturn(aggRow);

        daySalesApi.calculateDaySales();

        // verify called with business day boundaries
        verify(daySalesDao).selectInvoicedSalesAggregatesForDay(zdtCaptor.capture(), zdtCaptor.capture());

        ZonedDateTime start = zdtCaptor.getAllValues().get(0);
        ZonedDateTime end = zdtCaptor.getAllValues().get(1);

        assertEquals(IST, start.getZone());
        assertEquals(IST, end.getZone());
        assertEquals(start.toLocalDate().atStartOfDay(IST), start);
        assertEquals(start.plusDays(1), end);

        // verify insert called with a DaySales built from conversion
        verify(daySalesDao).insert(daySalesCaptor.capture());
        DaySales inserted = daySalesCaptor.getValue();
        assertNotNull(inserted);

        // We don't know exact DaySalesConversion implementation, but minimally date must match start
        assertEquals(start, inserted.getDate());

        // If your DaySalesConversion sets these fields from row, these assertions should pass.
        // If it uses different numeric types internally, adjust casts in conversion or here.
        assertEquals(2, inserted.getInvoicedOrdersCount());
        assertEquals(5, inserted.getInvoicedItemsCount());
        assertEquals(250.75, inserted.getTotalRevenue());
    }

    @Test
    void calculateDaySales_shouldStillInsert_whenDaoReturnsNullsInRow() throws Exception {
        // defensive: conversion might treat nulls as 0
        Object[] aggRow = new Object[] { null, null, null };
        when(daySalesDao.selectInvoicedSalesAggregatesForDay(any(), any())).thenReturn(aggRow);

        daySalesApi.calculateDaySales();

        verify(daySalesDao).insert(any(DaySales.class));
    }
}
