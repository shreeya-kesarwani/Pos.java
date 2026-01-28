package com.pos.model.data;

import com.pos.pojo.DaySales;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class DaySalesData {
    private LocalDate date;
    private Integer invoicedOrdersCount;
    private Integer invoicedItemsCount;
    private BigDecimal totalRevenue;
}
