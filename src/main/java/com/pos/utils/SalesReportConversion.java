package com.pos.utils;

import com.pos.model.data.SalesReportData;
import com.pos.pojo.SalesReport;

import java.util.ArrayList;
import java.util.List;

public class SalesReportConversion {

    private SalesReportConversion() {}

    public static List<SalesReportData> toData(List<SalesReport> rows) {
        if (rows == null || rows.isEmpty()) return List.of();

        List<SalesReportData> out = new ArrayList<>(rows.size());
        for (SalesReport r : rows) {
            SalesReportData d = new SalesReportData();
            d.setBarcode(r.getBarcode());
            d.setProductName(r.getProductName());
            d.setQuantity(r.getQuantity());
            d.setRevenue(r.getRevenue());
            out.add(d);
        }
        return out;
    }
}
