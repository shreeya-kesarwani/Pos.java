//package com.pos.pojo;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.ZonedDateTime;
//
//@Entity
//@Table(
//        uniqueConstraints = {
//                @UniqueConstraint(name = "DaySales_date_uk",columnNames = {"date"})
//        }
//)
//@Getter
//@Setter
//public class DaySales {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private ZonedDateTime date;
//
//    @Column(nullable = false)
//    private Integer invoicedOrdersCount;
//
//    @Column(nullable = false)
//    private Integer invoicedItemsCount;
//
//    @Column(nullable = false)
//    private double totalRevenue;
//}

package com.pos.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "pos_day_sales",
        uniqueConstraints = {
                @UniqueConstraint(name = "DaySales_date_uk", columnNames = {"date"})
        }
)
@Getter
@Setter
public class DaySales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date", nullable = false)
    private ZonedDateTime date;

    @Column(name = "invoiced_orders_count", nullable = false)
    private Integer invoicedOrdersCount;

    @Column(name = "invoiced_items_count", nullable = false)
    private Integer invoicedItemsCount;

    @Column(name = "total_revenue", nullable = false)
    private double totalRevenue;
}
