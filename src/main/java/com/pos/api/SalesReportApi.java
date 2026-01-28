package com.pos.api;

import com.pos.dao.SalesReportDao;
import com.pos.dto.SalesReportDto;
import com.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class SalesReportApi {

    @Autowired
    private SalesReportDao salesReportDao;

    @Transactional(readOnly = true)
    public List<Object[]> getSalesReport(SalesReportDto dto) {
        return salesReportDao.getSalesReportRows(
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getClientId()
        );
    }

    @Transactional(readOnly = true)
    public List<Object[]> getCheckSalesReport(SalesReportDto dto) throws ApiException {
        List<Object[]> rows = getSalesReport(dto);
        if (rows == null || rows.isEmpty()) {
            throw new ApiException(String.format(
                    "No sales report data found between %s and %s",
                    dto.getStartDate(), dto.getEndDate()
            ));
        }
        return rows;
    }
}
