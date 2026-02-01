package com.pos.api;

import com.pos.dao.SalesReportDao;
import com.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class SalesReportApi {

    @Autowired
    private SalesReportDao salesReportDao;

    @Transactional(readOnly = true)
    public List<Object[]> getSalesReport(LocalDate startDate, LocalDate endDate, Integer clientId) {
        return salesReportDao.getSalesReportRows(startDate, endDate, clientId);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getCheckSalesReport(LocalDate startDate, LocalDate endDate, Integer clientId) throws ApiException {
        List<Object[]> rows = getSalesReport(startDate, endDate, clientId);

        if (rows == null || rows.isEmpty()) {
            throw new ApiException(String.format(
                    "No sales report data found between %s and %s",
                    startDate, endDate
            ));
        }
        return rows;
    }
}
