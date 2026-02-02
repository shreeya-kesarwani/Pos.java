package com.pos.dto;

import com.pos.api.SalesReportApi;
import com.pos.exception.ApiException;
import com.pos.model.data.SalesReportData;
import com.pos.model.form.SalesReportForm;
import com.pos.pojo.SalesReport;
import com.pos.utils.SalesReportConversion;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class SalesReportDto extends AbstractDto {

    private final SalesReportApi salesReportApi;

    public SalesReportDto(SalesReportApi salesReportApi) {
        this.salesReportApi = salesReportApi;
    }

    private void validate(SalesReportForm form) throws ApiException {
        validateForm(form);
        normalize(form);

        LocalDate start = form.getStartDate();
        LocalDate end = form.getEndDate();

        if (start != null && end != null && start.isAfter(end)) {
            throw new ApiException("startDate cannot be after endDate");
        }
    }

    public List<SalesReportData> get(SalesReportForm form) throws ApiException {
        validate(form);

        List<SalesReport> rows = salesReportApi.getSalesReport(
                form.getStartDate(),
                form.getEndDate(),
                form.getClientId()
        );

        return SalesReportConversion.toData(rows);
    }

    public List<SalesReportData> getCheck(SalesReportForm form) throws ApiException {
        validate(form);

        List<SalesReport> rows = salesReportApi.getCheckSalesReport(
                form.getStartDate(),
                form.getEndDate(),
                form.getClientId()
        );

        return SalesReportConversion.toData(rows);
    }
}
