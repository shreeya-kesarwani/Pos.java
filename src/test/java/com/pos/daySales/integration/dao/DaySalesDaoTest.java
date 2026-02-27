package com.pos.daySales.integration.dao;

import com.pos.dao.DaySalesDao;
import com.pos.dao.OrderDao;
import com.pos.dao.OrderItemDao;
import com.pos.model.constants.OrderStatus;
import com.pos.pojo.DaySales;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({DaySalesDao.class, OrderDao.class, OrderItemDao.class})
class DaySalesDaoTest extends AbstractDaoTest {

    @Autowired
    private DaySalesDao dao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

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
        dao.insert(TestEntities.newDaySales(baseMidnightUtc.minusDays(2), 1, 2, 100.0));
        DaySales d1 = TestEntities.newDaySales(baseMidnightUtc.minusDays(1), 2, 3, 200.0);
        DaySales d2 = TestEntities.newDaySales(baseMidnightUtc, 3, 4, 300.0);
        dao.insert(d1);
        dao.insert(d2);
        dao.insert(TestEntities.newDaySales(baseMidnightUtc.plusDays(1), 4, 5, 400.0));
        em.clear();

        List<DaySales> out = dao.selectInRange(baseMidnightUtc.minusDays(1), baseMidnightUtc.plusDays(1));

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(d1.getId())));
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(d2.getId())));
    }

    @Test
    void selectInvoicedSalesAggregatesForDayWhenMixedOrdersExist() {
        Order o1 = TestEntities.newOrder(OrderStatus.INVOICED, "inv");
        orderDao.insert(o1);

        OrderItem oi1 = new OrderItem();
        oi1.setOrderId(o1.getId());
        oi1.setProductId(101);
        oi1.setQuantity(2);
        oi1.setSellingPrice(50.0);
        orderItemDao.insert(oi1);

        Order o2 = TestEntities.newOrder(OrderStatus.CREATED, "inv");
        orderDao.insert(o2);

        OrderItem oi2 = new OrderItem();
        oi2.setOrderId(o2.getId());
        oi2.setProductId(102);
        oi2.setQuantity(5);
        oi2.setSellingPrice(10.0);
        orderItemDao.insert(oi2);

        em.clear();

        Object[] agg = dao.selectInvoicedSalesAggregatesForDay(baseUtc.minusHours(1), baseUtc.plusHours(1));

        assertEquals(1L, ((Number) agg[0]).longValue());
        assertEquals(2L, ((Number) agg[1]).longValue());
        assertEquals(100.0, ((Number) agg[2]).doubleValue(), 0.0001);
    }
}