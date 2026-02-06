package com.pos.utils;

import com.pos.model.data.DaySalesData;
import com.pos.pojo.DaySales;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class DaySalesConversion {

    public static DaySalesData toData(DaySales pojo) {
        DaySalesData data = new DaySalesData();
        data.setDate(pojo.getDate());
        data.setInvoicedOrdersCount(pojo.getInvoicedOrdersCount());
        data.setInvoicedItemsCount(pojo.getInvoicedItemsCount());
        data.setTotalRevenue(pojo.getTotalRevenue());
        return data;
    }

    public static List<DaySalesData> toData(List<DaySales> pojos) {
        return pojos.stream()
                .map(DaySalesConversion::toData)
                .collect(Collectors.toList());
    }

    public static DaySales toPojo(
            ZonedDateTime businessDayStart,
            Object ordersCount,
            Object itemsCount,
            Object revenue
    ) {
        DaySales pojo = new DaySales();
        pojo.setDate(businessDayStart);
        pojo.setInvoicedOrdersCount(toInt(ordersCount));
        pojo.setInvoicedItemsCount(toInt(itemsCount));
        pojo.setTotalRevenue(toBigDecimal(revenue).doubleValue());
        return pojo;
    }

    public static int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }

    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        return new BigDecimal(value.toString());
    }
}
