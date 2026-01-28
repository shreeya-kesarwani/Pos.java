package com.pos.api;

import com.pos.dao.DaySalesDao;
import com.pos.dto.DaySalesDto;
import com.pos.exception.ApiException;
import com.pos.pojo.DaySales;
import com.pos.utils.DaySalesConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class DaySalesApi {

    @Autowired
    private DaySalesDao daySalesDao;

    @Transactional(readOnly = true)
    public List<DaySales> getDaySales(DaySalesDto dto) {
        return daySalesDao.selectBetweenDates(dto.getStartDate(), dto.getEndDate());
    }

    @Transactional(readOnly = true)
    public List<DaySales> getCheckDaySales(DaySalesDto dto) throws ApiException {

        List<DaySales> daySales = getDaySales(dto);

        if (daySales == null || daySales.isEmpty()) {
            throw new ApiException(String.format(
                    "No day sales data found between %s and %s",
                    dto.getStartDate(), dto.getEndDate()
            ));
        }
        return daySales;
    }

    public void calculateAndStore(LocalDate date) {

        Object[] row = daySalesDao.getAggregatesForDate(date);

        int ordersCount = DaySalesConversion.toInt(row[0]);
        int itemsCount = DaySalesConversion.toInt(row[1]);
        BigDecimal revenue = DaySalesConversion.toBigDecimal(row[2]);

        DaySales pojo = new DaySales();
        pojo.setDate(date);
        pojo.setInvoicedOrdersCount(ordersCount);
        pojo.setInvoicedItemsCount(itemsCount);
        pojo.setTotalRevenue(revenue);

        daySalesDao.insertOrUpdate(pojo);
    }

    public void calculateAndStore(LocalDate startDate, LocalDate endDate) throws ApiException {
        if (startDate.isAfter(endDate)) {
            throw new ApiException("startDate cannot be after endDate");
        }
        LocalDate d = startDate;
        while (!d.isAfter(endDate)) {
            calculateAndStore(d);
            d = d.plusDays(1);
        }
    }
}
