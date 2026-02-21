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

import static com.pos.model.constants.ErrorMessages.START_DATE_AFTER_END_DATE;

@Component
public class SalesReportDto extends AbstractDto {

    @Autowired
    SalesReportApi salesReportApi;

    public List<SalesReportData> getCheck(SalesReportForm form) throws ApiException {
        validate(form);
        List<SalesReportData> rows = salesReportApi.getCheckSalesReport(form.getStartDate(), form.getEndDate(), form.getClientId());
        return SalesReportConversion.toData(rows);
    }

    private void validate(SalesReportForm form) throws ApiException {
        normalize(form);
        validateForm(form);

        LocalDate start = form.getStartDate();
        LocalDate end = form.getEndDate();

        if (start != null && end != null && start.isAfter(end)) {
            throw new ApiException(START_DATE_AFTER_END_DATE.value() + " | startDate=" + start + ", endDate=" + end);
        }
    }
}
