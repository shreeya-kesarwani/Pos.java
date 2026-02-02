package com.pos.dto;

import com.pos.api.DaySalesApi;
import com.pos.exception.ApiException;
import com.pos.model.data.DaySalesData;
import com.pos.model.form.DaySalesForm;
import com.pos.pojo.DaySales;
import com.pos.utils.DaySalesConversion;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class DaySalesDto extends AbstractDto {

    private final DaySalesApi daySalesApi;

    public DaySalesDto(DaySalesApi daySalesApi) {
        this.daySalesApi = daySalesApi;
    }

    private void validate(DaySalesForm form) throws ApiException {
        normalize(form);
        validateForm(form);

        ZonedDateTime start = form.getStartDate();
        ZonedDateTime end = form.getEndDate();

        if (start == null || end == null) {
            throw new ApiException("startDate and endDate are required");
        }
        if (start.isAfter(end)) {
            throw new ApiException("startDate cannot be after endDate");
        }
    }

    public List<DaySalesData> get(DaySalesForm form) throws ApiException {
        validate(form);
        List<DaySales> pojos = daySalesApi.getDaySales(form.getStartDate(), form.getEndDate());
        return DaySalesConversion.toData(pojos);
    }

    public List<DaySalesData> getCheck(DaySalesForm form) throws ApiException {
        validate(form);
        List<DaySales> pojos = daySalesApi.getCheckDaySales(form.getStartDate(), form.getEndDate());
        return DaySalesConversion.toData(pojos);
    }
}
