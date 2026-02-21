package com.pos.api;

import com.pos.dao.SalesReportDao;
import com.pos.exception.ApiException;
import com.pos.model.data.SalesReportData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;

import static com.pos.model.constants.ErrorMessages.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class SalesReportApi {

    @Autowired
    private SalesReportDao salesReportDao;

    public List<SalesReportData> getSalesReport(LocalDate startDate, LocalDate endDate, Integer clientId) {
        return salesReportDao.getSalesReportRows(startDate, endDate, clientId);
    }

    public List<SalesReportData> getCheckSalesReport(LocalDate startDate, LocalDate endDate, Integer clientId) throws ApiException {

        List<SalesReportData> rows = getSalesReport(startDate, endDate, clientId);
        if (CollectionUtils.isEmpty(rows)) {
            throw new ApiException(
                    SALES_REPORT_EMPTY.value() +
                            " | startDate=" + startDate +
                            ", endDate=" + endDate +
                            ", clientId=" + clientId
            );
        }
        return rows;
    }
}