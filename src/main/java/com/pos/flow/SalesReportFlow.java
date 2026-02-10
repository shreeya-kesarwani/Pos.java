package com.pos.flow;

import com.pos.api.ClientApi;
import com.pos.api.SalesReportApi;
import com.pos.exception.ApiException;
import com.pos.model.data.SalesReportData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Transactional(rollbackFor = ApiException.class)
public class SalesReportFlow {

    @Autowired private ClientApi clientApi;
    @Autowired private SalesReportApi salesReportApi;

    @Transactional(readOnly = true)
    public List<SalesReportData> getCheckSalesReport(LocalDate startDate, LocalDate endDate, Integer clientId) throws ApiException {
        return salesReportApi.getCheckSalesReport(startDate, endDate, clientId);
    }
}
