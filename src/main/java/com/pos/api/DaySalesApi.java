package com.pos.api;

import com.pos.dao.DaySalesDao;
import com.pos.exception.ApiException;
import com.pos.pojo.DaySales;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class DaySalesApi {

    private final DaySalesDao daySalesDao;

    public DaySalesApi(DaySalesDao daySalesDao) {
        this.daySalesDao = daySalesDao;
    }

    @Transactional(readOnly = true)
    public List<DaySales> getDaySales(ZonedDateTime startDate, ZonedDateTime endDate) throws ApiException {
        if (startDate == null || endDate == null) {
            throw new ApiException("startDate and endDate are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new ApiException("startDate cannot be after endDate");
        }

        ZonedDateTime startUtc = toUtcStartOfDay(startDate);
        ZonedDateTime endUtcExclusive = toUtcStartOfNextDay(endDate);
        List<DaySales> rows = daySalesDao.selectBetweenDates(startUtc, endUtcExclusive);
        return rows == null ? List.of() : rows;
    }


    @Transactional(readOnly = true)
    public List<DaySales> getCheckDaySales(ZonedDateTime startDate, ZonedDateTime endDate) throws ApiException {
        return getDaySales(startDate, endDate);
    }

    public void calculateAndStore(ZonedDateTime dayInAnyZone) throws ApiException {
        if (dayInAnyZone == null) throw new ApiException("date is required");

        ZonedDateTime dayStartUtc = toUtcStartOfDay(dayInAnyZone);

        Object[] row = daySalesDao.getAggregatesForDate(dayStartUtc);

        int ordersCount = toInt(row, 0);
        int itemsCount = toInt(row, 1);
        double revenue = toDouble(row, 2);

        DaySales pojo = new DaySales();
        pojo.setDate(dayStartUtc);
        pojo.setInvoicedOrdersCount(ordersCount);
        pojo.setInvoicedItemsCount(itemsCount);
        pojo.setTotalRevenue(revenue);

        daySalesDao.insertOrUpdate(pojo);
    }

    private static double toDouble(Object[] row, int idx) {
        if (row == null || row.length <= idx || row[idx] == null) return 0.0;
        Object v = row[idx];
        if (v instanceof Number n) return n.doubleValue();
        return 0.0;
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

    private static int toInt(Object[] row, int idx) {
        if (row == null || row.length <= idx || row[idx] == null) return 0;
        return ((Number) row[idx]).intValue();
    }

    private static BigDecimal toBigDecimal(Object[] row, int idx) {
        if (row == null || row.length <= idx || row[idx] == null) return BigDecimal.ZERO;
        Object v = row[idx];
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }
}
