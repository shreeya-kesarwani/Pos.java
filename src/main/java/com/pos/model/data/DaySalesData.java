package com.pos.model.data;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class DaySalesData {
    private ZonedDateTime date;
    private Integer invoicedOrdersCount;
    private Integer invoicedItemsCount;
    private Double totalRevenue;
}
