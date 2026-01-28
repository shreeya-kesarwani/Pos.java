package com.pos.pojo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity

@Getter
@Setter
public class DaySales {

    @Id
    private LocalDate date;

    @Column(nullable = false)
    private Integer invoicedOrdersCount;

    @Column(nullable = false)
    private Integer invoicedItemsCount;

    @Column(nullable = false)
    private BigDecimal totalRevenue;
}
