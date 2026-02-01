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

        ZonedDateTime startUtc = toUtcStartOfDay(startDate);
        ZonedDateTime endUtcExclusive = toUtcStartOfNextDay(endDate);

        return daySalesDao.selectBetweenDates(startUtc, endUtcExclusive);
    }

    @Transactional(readOnly = true)
    public List<DaySales> getCheckDaySales(ZonedDateTime startDate, ZonedDateTime endDate) throws ApiException {
        List<DaySales> rows = getDaySales(startDate, endDate);
        if (rows == null || rows.isEmpty()) {
            throw new ApiException(String.format(
                    "No day sales data found between %s and %s",
                    startDate, endDate
            ));
        }
        return rows;
    }

    /**
     * Scheduler calls this.
     * The input can be in ANY timezone; we normalize to UTC day boundaries inside.
     */
    public void calculateAndStore(ZonedDateTime dayInAnyZone) throws ApiException {
        if (dayInAnyZone == null) throw new ApiException("date is required");

        ZonedDateTime dayStartUtc = toUtcStartOfDay(dayInAnyZone);

        Object[] row = daySalesDao.getAggregatesForDate(dayStartUtc);

        int ordersCount = toInt(row, 0);
        int itemsCount = toInt(row, 1);
        double revenue = toDouble(row, 2);   // ✅ convert instead of BigDecimal

        DaySales pojo = new DaySales();
        pojo.setDate(dayStartUtc);
        pojo.setInvoicedOrdersCount(ordersCount);
        pojo.setInvoicedItemsCount(itemsCount);
        pojo.setTotalRevenue(revenue);       // ✅ now matches double

        daySalesDao.insertOrUpdate(pojo);
    }

    private static double toDouble(Object[] row, int idx) {
        if (row == null || row.length <= idx || row[idx] == null) return 0.0;

        Object v = row[idx];

        if (v instanceof Number n) return n.doubleValue();   // handles BigDecimal too
        return 0.0;
    }


    // ---------------- helpers ----------------

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
