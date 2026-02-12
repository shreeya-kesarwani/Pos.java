package com.pos.api;

import com.pos.dao.DaySalesDao;
import com.pos.exception.ApiException;
import com.pos.pojo.DaySales;
import com.pos.utils.DaySalesConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static com.pos.model.constants.ErrorMessages.DATE_REQUIRED;

@Service
@Transactional(rollbackFor = Exception.class)
public class DaySalesApi {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");

    @Autowired
    private DaySalesDao daySalesDao;

    @Transactional(readOnly = true)
    public List<DaySales> getDaySales(ZonedDateTime dayInAnyZone) throws ApiException {
        if (dayInAnyZone == null) {
            throw new ApiException(DATE_REQUIRED.value());
        }

        ZonedDateTime dayStartBusiness = toBusinessStartOfDay(dayInAnyZone);
        ZonedDateTime dayEndBusiness = dayStartBusiness.plusDays(1);

        List<DaySales> rows = daySalesDao.selectInRange(dayStartBusiness, dayEndBusiness);
        return rows == null ? List.of() : rows;
    }

    public void calculateDaySales() throws ApiException {
        ZonedDateTime dayStartBusiness = ZonedDateTime.now(BUSINESS_ZONE)
                .toLocalDate()
                .atStartOfDay(BUSINESS_ZONE);

        ZonedDateTime dayEndBusiness = dayStartBusiness.plusDays(1);
        calculateDaySales(dayStartBusiness, dayEndBusiness);
    }

    private void calculateDaySales(ZonedDateTime startBusiness, ZonedDateTime endBusiness) throws ApiException {
        Object[] row = daySalesDao.selectInvoicedSalesAggregatesForDay(startBusiness, endBusiness);
        DaySales pojo = DaySalesConversion.toPojo(
                startBusiness,
                row[0],
                row[1],
                row[2]
        );
        daySalesDao.insert(pojo);
    }

    private static ZonedDateTime toBusinessStartOfDay(ZonedDateTime zdt) {
        return zdt.withZoneSameInstant(BUSINESS_ZONE)
                .toLocalDate()
                .atStartOfDay(BUSINESS_ZONE);
    }
}
