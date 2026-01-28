package com.pos.dto;

import com.pos.api.DaySalesApi;
import com.pos.exception.ApiException;
import com.pos.model.data.DaySalesData;
import com.pos.model.form.DaySalesForm;
import com.pos.utils.DaySalesConversion;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Getter
@Component
public class DaySalesDto extends AbstractDto {

    @Autowired
    private DaySalesApi daySalesApi;

    private LocalDate startDate;
    private LocalDate endDate;

    public void validate_from(DaySalesForm form) throws ApiException {

        // Bean validation (@NotNull, etc.)
        validateForm(form);

        LocalDate start = form.getStartDate();
        LocalDate end = form.getEndDate();

        if (start.isAfter(end)) {
            throw new ApiException("startDate cannot be after endDate");
        }

        this.startDate = start;
        this.endDate = end;
    }

    public List<DaySalesData> get() {
        return DaySalesConversion.toData(
                daySalesApi.getDaySales(this)
        );
    }

    public List<DaySalesData> getCheck() throws ApiException {
        return DaySalesConversion.toData(
                daySalesApi.getCheckDaySales(this)
        );
    }
}
