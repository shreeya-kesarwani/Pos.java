package com.pos.api;

import com.pos.dao.DaySalesDao;
import com.pos.exception.ApiException;
import com.pos.pojo.DaySales;
import com.pos.utils.DaySalesConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static com.pos.model.constants.ErrorMessages.*;

@Service
@Transactional(rollbackFor = ApiException.class)
public class DaySalesApi {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");

    @Autowired
    DaySalesDao daySalesDao;

    @Transactional(readOnly = true)
    public List<DaySales> getDaySales(ZonedDateTime startDate) throws ApiException {

        ZonedDateTime startUtc = toUtcStartOfDay(startDate);

        List<DaySales> rows = daySalesDao.selectInRange(startUtc);
        return rows == null ? List.of() : rows;
    }

    public void calculateDaySales() throws ApiException {
        ZonedDateTime startBusiness =
                ZonedDateTime.now(BUSINESS_ZONE)
                        .toLocalDate()
                        .atStartOfDay(BUSINESS_ZONE);

        calculateDaySales(startBusiness);
    }

    public void calculateDaySales(ZonedDateTime dayInAnyZone) throws ApiException {

        if (dayInAnyZone == null) {
            throw new ApiException(DATE_REQUIRED.value());
        }

        ZonedDateTime dayStartBusiness = dayInAnyZone
                .withZoneSameInstant(BUSINESS_ZONE)
                .toLocalDate()
                .atStartOfDay(BUSINESS_ZONE);

        Object[] row = daySalesDao.selectInvoicedSalesAggregatesForDay(dayStartBusiness);

        DaySales pojo = DaySalesConversion.toPojo(
                dayStartBusiness,
                row[0],
                row[1],
                row[2]
        );

        daySalesDao.insert(pojo);
    }

    private static ZonedDateTime toUtcStartOfDay(ZonedDateTime zdt) {
        return zdt.withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC);
    }

    private static ZonedDateTime toUtcStartOfNextDay(ZonedDateTime zdt) {
        return zdt.withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDate()
                .plusDays(1)
                .atStartOfDay(ZoneOffset.UTC);
    }
}
