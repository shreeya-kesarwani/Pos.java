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
        uniqueConstraints = {
                @UniqueConstraint(name = "pos_daySales_date_uk", columnNames = {"date"})
        }
)
@Getter
@Setter
public class DaySales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private ZonedDateTime date;

    @Column(nullable = false)
    private Integer invoicedOrdersCount;

    @Column(nullable = false)
    private Integer invoicedItemsCount;

    @Column(nullable = false)
    private Double totalRevenue;
}
