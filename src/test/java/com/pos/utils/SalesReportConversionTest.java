package com.pos.utils;

import com.pos.model.data.SalesReportData;
import com.pos.utils.SalesReportConversion;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SalesReportConversionTest {

    private SalesReportData row(String barcode, String name, Integer qty, Double revenue) {
        SalesReportData r = new SalesReportData();
        r.setBarcode(barcode);
        r.setProductName(name);
        r.setQuantity(qty);
        r.setRevenue(revenue);
        return r;
    }

    @Test
    void toData_shouldReturnEmptyList_whenInputNull() {
        List<SalesReportData> result = SalesReportConversion.toData(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toData_shouldReturnEmptyList_whenInputEmpty() {
        List<SalesReportData> result = SalesReportConversion.toData(List.of());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toData_shouldMapFieldsCorrectly() {
        SalesReportData r1 = row("b1", "prod1", 5, 100.0);
        SalesReportData r2 = row("b2", "prod2", 3, 60.0);

        List<SalesReportData> input = Arrays.asList(r1, r2);
        List<SalesReportData> result = SalesReportConversion.toData(input);

        assertEquals(2, result.size());

        assertEquals("b1", result.get(0).getBarcode());
        assertEquals("prod1", result.get(0).getProductName());
        assertEquals(5, result.get(0).getQuantity());
        assertEquals(100.0, result.get(0).getRevenue());

        assertEquals("b2", result.get(1).getBarcode());
        assertEquals("prod2", result.get(1).getProductName());
        assertEquals(3, result.get(1).getQuantity());
        assertEquals(60.0, result.get(1).getRevenue());
    }

    @Test
    void toData_shouldCreateNewObjects_notSameReference() {
        SalesReportData r = row("b1", "prod1", 5, 100.0);

        List<SalesReportData> result = SalesReportConversion.toData(List.of(r));

        assertNotSame(r, result.get(0));
    }
}
