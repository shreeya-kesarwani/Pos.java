package com.pos.daySales.integration.dao;

import com.pos.dao.DaySalesDao;
import com.pos.model.constants.OrderStatus;
import com.pos.pojo.DaySales;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({DaySalesDao.class, TestFactory.class})
class DaySalesDaoTest extends AbstractDaoTest {

    @Autowired
    private DaySalesDao dao;

    @Autowired
    private TestFactory testFactory;

    private ZonedDateTime baseMidnightUtc;
    private ZonedDateTime baseUtc;

    @BeforeEach
    void setupData() {
        baseMidnightUtc = ZonedDateTime.now(ZoneId.of("UTC"))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        baseUtc = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    @Test
    void selectInRangeWhenRowsExist() {
        testFactory.createDaySales(baseMidnightUtc.minusDays(2), 1, 2, 100.0);
        DaySales d1 = testFactory.createDaySales(baseMidnightUtc.minusDays(1), 2, 3, 200.0);
        DaySales d2 = testFactory.createDaySales(baseMidnightUtc, 3, 4, 300.0);
        testFactory.createDaySales(baseMidnightUtc.plusDays(1), 4, 5, 400.0);
        em.clear();

        // DAO expects (start, end)
        List<DaySales> out = dao.selectInRange(baseMidnightUtc.minusDays(1), baseMidnightUtc.plusDays(1));

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(d1.getId())));
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(d2.getId())));
    }

    @Test
    void selectInvoicedSalesAggregatesForDayWhenMixedOrdersExist() {
        Order o1 = testFactory.createOrder(OrderStatus.INVOICED, "inv");
        OrderItem oi1 = new OrderItem();
        oi1.setProductId(101);
        oi1.setQuantity(2);
        oi1.setSellingPrice(50.0);
        testFactory.createOrderItems(o1.getId(), List.of(oi1));

        Order o2 = testFactory.createOrder(OrderStatus.CREATED, "inv");
        OrderItem oi2 = new OrderItem();
        oi2.setProductId(102);
        oi2.setQuantity(5);
        oi2.setSellingPrice(10.0);
        testFactory.createOrderItems(o2.getId(), List.of(oi2));

        em.clear();

        Object[] agg = dao.selectInvoicedSalesAggregatesForDay(baseUtc.minusHours(1), baseUtc.plusHours(1));

        assertEquals(1L, ((Number) agg[0]).longValue());                 // invoiced orders count
        assertEquals(2L, ((Number) agg[1]).longValue());                 // invoiced items count
        assertEquals(100.0, ((Number) agg[2]).doubleValue(), 0.0001);    // revenue
    }
}