package com.pos.dto;

import com.pos.api.DaySalesApi;
import com.pos.exception.ApiException;
import com.pos.model.data.DaySalesData;
import com.pos.model.form.DaySalesForm;
import com.pos.pojo.DaySales;

import com.pos.utils.DaySalesConversion;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

import static com.pos.model.constants.ErrorMessages.START_AND_END_DATE_REQUIRED;

@Component
public class DaySalesDto extends AbstractDto {

    @Autowired
    private DaySalesApi daySalesApi;

    private void validate(@Valid DaySalesForm form) throws ApiException {
        normalize(form);

        ZonedDateTime start = form.getStartDate();
        if (start == null) {
            throw new ApiException(START_AND_END_DATE_REQUIRED.value());
        }
    }

    public List<DaySalesData> get(DaySalesForm form) throws ApiException {
        validate(form);

        List<DaySales> daySalesList =
                daySalesApi.getDaySales(form.getStartDate());

        return DaySalesConversion.toData(daySalesList);
    }
}
