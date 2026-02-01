package com.pos.dto;

import com.pos.api.SalesReportApi;
import com.pos.exception.ApiException;
import com.pos.model.data.SalesReportData;
import com.pos.model.form.SalesReportForm;
import com.pos.utils.SalesReportConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class SalesReportDto extends AbstractDto {

    @Autowired
    private SalesReportApi salesReportApi;

    private void validate(SalesReportForm form) throws ApiException {
        // Trim all String fields in the form
        normalize(form);

        // Bean validation (@NotNull, etc.)
        super.validateForm(form);

        LocalDate start = form.getStartDate();
        LocalDate end = form.getEndDate();

        if (start.isAfter(end)) {
            throw new ApiException("startDate cannot be after endDate");
        }
    }

    public List<SalesReportData> get(SalesReportForm form) throws ApiException {
        validate(form);

        List<Object[]> rows =
                salesReportApi.getSalesReport(
                        form.getStartDate(),
                        form.getEndDate(),
                        form.getClientId()
                );

        return SalesReportConversion.toData(rows);
    }

    public List<SalesReportData> getCheck(SalesReportForm form) throws ApiException {
        validate(form);

        List<Object[]> rows =
                salesReportApi.getCheckSalesReport(
                        form.getStartDate(),
                        form.getEndDate(),
                        form.getClientId()
                );

        return SalesReportConversion.toData(rows);
    }
}
