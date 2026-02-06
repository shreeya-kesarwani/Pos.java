package com.pos.dto;

import com.pos.api.DaySalesApi;
import com.pos.exception.ApiException;
import com.pos.model.data.DaySalesData;
import com.pos.model.form.DaySalesForm;
import com.pos.pojo.DaySales;
import com.pos.utils.DaySalesConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

import static com.pos.model.constants.ErrorMessages.START_DATE_AFTER_END_DATE;
import static com.pos.model.constants.ErrorMessages.START_AND_END_DATE_REQUIRED; // <-- add in enum

@Component
public class DaySalesDto extends AbstractDto {

    @Autowired
    DaySalesApi daySalesApi;

    private void validate(DaySalesForm form) throws ApiException {
        normalize(form);
        validateForm(form);

        ZonedDateTime start = form.getStartDate();
        ZonedDateTime end = form.getEndDate();

        if (start == null || end == null) {
            throw new ApiException(START_AND_END_DATE_REQUIRED.value() + " | startDate=" + start + ", endDate=" + end);
        }
        if (start.isAfter(end)) {
            throw new ApiException(START_DATE_AFTER_END_DATE.value() + " | startDate=" + start + ", endDate=" + end);
        }
    }

    public List<DaySalesData> get(DaySalesForm form) throws ApiException {
        validate(form);
        List<DaySales> pojos = daySalesApi.getDaySales(form.getStartDate(), form.getEndDate());
        return DaySalesConversion.toData(pojos);
    }
}
