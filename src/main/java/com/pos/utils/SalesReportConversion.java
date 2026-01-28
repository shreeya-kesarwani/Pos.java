package com.pos.utils;

import com.pos.model.data.SalesReportData;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class SalesReportConversion {

    private SalesReportConversion() {}

    // row = {barcode, productName, quantity, revenue}
    public static SalesReportData toData(Object[] row) {
        SalesReportData data = new SalesReportData();
        data.setBarcode((String) row[0]);
        data.setProductName((String) row[1]);
        data.setQuantity(toInt(row[2]));
        data.setRevenue(toBigDecimal(row[3]));
        return data;
    }

    public static List<SalesReportData> toData(List<Object[]> rows) {
        return rows.stream().map(SalesReportConversion::toData).collect(Collectors.toList());
    }

    private static int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        return new BigDecimal(value.toString());
    }
}
