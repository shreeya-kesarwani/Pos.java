package com.pos.utils;

import com.pos.model.data.SalesReportData;

import java.util.ArrayList;
import java.util.List;

public class SalesReportConversion {

    private SalesReportConversion() {}

    public static List<com.pos.model.data.SalesReportData> toData(List<SalesReportData> rows) {
        if (rows == null || rows.isEmpty()) return List.of();

        List<com.pos.model.data.SalesReportData> out = new ArrayList<>(rows.size());
        for (SalesReportData r : rows) {
            com.pos.model.data.SalesReportData d = new com.pos.model.data.SalesReportData();
            d.setBarcode(r.getBarcode());
            d.setProductName(r.getProductName());
            d.setQuantity(r.getQuantity());
            d.setRevenue(r.getRevenue());
            out.add(d);
        }
        return out;
    }
}
