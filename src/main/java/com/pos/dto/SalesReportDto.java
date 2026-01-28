package com.pos.dto;

import com.pos.api.SalesReportApi;
import com.pos.exception.ApiException;
import com.pos.model.data.SalesReportData;
import com.pos.model.form.SalesReportForm;
import com.pos.utils.SalesReportConversion;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Getter
@Component
public class SalesReportDto extends AbstractDto {

    @Autowired
    private SalesReportApi salesReportApi;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer clientId; // nullable

    public void validate_form(SalesReportForm form) throws ApiException {

        // Bean validation (@NotNull on dates)
        validateForm(form);

        LocalDate start = form.getStartDate();
        LocalDate end = form.getEndDate();

        if (start.isAfter(end)) {
            throw new ApiException("startDate cannot be after endDate");
        }

        this.startDate = start;
        this.endDate = end;
        this.clientId = form.getClientId();
    }

    public List<SalesReportData> get() {
        return SalesReportConversion.toData(salesReportApi.getSalesReport(this));
    }

    public List<SalesReportData> getCheck() throws ApiException {
        return SalesReportConversion.toData(salesReportApi.getCheckSalesReport(this));
    }
}
