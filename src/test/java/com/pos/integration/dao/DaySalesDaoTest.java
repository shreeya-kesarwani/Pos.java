package com.pos.integration.dao;

import com.pos.dao.DaySalesDao;
import com.pos.integration.setup.AbstractDaoTest;
import com.pos.integration.setup.TestEntities;
import com.pos.model.constants.OrderStatus;
import com.pos.pojo.DaySales;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(DaySalesDao.class)
class DaySalesDaoTest extends AbstractDaoTest {

    @Autowired
    private DaySalesDao dao;

    @Test
    void selectInRangeWhenRowsExist() {
        ZonedDateTime base = ZonedDateTime.now(ZoneId.of("UTC")).withHour(0).withMinute(0).withSecond(0).withNano(0);

        persist(TestEntities.daySales(base.minusDays(2), 1, 2, 100.0));
        DaySales d1 = persist(TestEntities.daySales(base.minusDays(1), 2, 3, 200.0));
        DaySales d2 = persist(TestEntities.daySales(base, 3, 4, 300.0));
        persist(TestEntities.daySales(base.plusDays(1), 4, 5, 400.0));
        em.clear();

        List<DaySales> out = dao.selectInRange(base.minusDays(1), base.plusDays(1));

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(d1.getId())));
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(d2.getId())));
    }

    @Test
    void selectInvoicedSalesAggregatesForDayWhenMixedOrdersExist() {
        ZonedDateTime base = ZonedDateTime.now(ZoneId.of("UTC"));

        Order o1 = persist(TestEntities.order(OrderStatus.INVOICED, "inv"));
        persist(TestEntities.orderItem(o1.getId(), 101, 2, 50.0));

        Order o2 = persist(TestEntities.order(OrderStatus.CREATED, "inv"));
        persist(TestEntities.orderItem(o2.getId(), 102, 5, 10.0));
        em.clear();

        Object[] agg = dao.selectInvoicedSalesAggregatesForDay(base.minusHours(1), base.plusHours(1));

        assertEquals(1L, ((Number) agg[0]).longValue());
        assertEquals(2L, ((Number) agg[1]).longValue());
        assertEquals(100.0, ((Number) agg[2]).doubleValue(), 0.0001);
    }
}