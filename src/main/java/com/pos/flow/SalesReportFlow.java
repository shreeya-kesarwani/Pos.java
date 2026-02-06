package com.pos.flow;

import com.pos.api.ClientApi;
import com.pos.api.SalesReportApi;
import com.pos.exception.ApiException;
import com.pos.model.data.SalesReportData;
import com.pos.pojo.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

import static com.pos.model.constants.ErrorMessages.CLIENT_NAME_NOT_FOUND;

@Component
@Transactional(rollbackFor = ApiException.class)
public class SalesReportFlow {

    @Autowired private ClientApi clientApi;
    @Autowired private SalesReportApi salesReportApi;

    @Transactional(readOnly = true)
    public List<SalesReportData> getSalesReport(LocalDate startDate, LocalDate endDate, String clientName) throws ApiException {

        Integer clientId = null;

        if (StringUtils.hasText(clientName)) {
            String normalizedClientName = clientName.trim();

            Client c = clientApi.getByName(normalizedClientName);
            if (c == null) {
                throw new ApiException(CLIENT_NAME_NOT_FOUND.value() + ": " + normalizedClientName);
            }
            clientId = c.getId();
        }

        return salesReportApi.getSalesReport(startDate, endDate, clientId);
    }

    @Transactional(readOnly = true)
    public List<SalesReportData> getCheckSalesReport(LocalDate startDate, LocalDate endDate, String clientName) throws ApiException {
        Integer clientId = clientApi.getCheckByName(clientName).getId();
        return salesReportApi.getCheckSalesReport(startDate, endDate, clientId);
    }
}
