package com.pos.integration.dao;

import com.pos.dao.SalesReportDao;
import com.pos.integration.setup.AbstractDaoTest;
import com.pos.integration.setup.TestEntities;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.SalesReportData;
import com.pos.pojo.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(SalesReportDao.class)
class SalesReportDaoTest extends AbstractDaoTest {

    @Autowired
    private SalesReportDao dao;

    @Test
    void getSalesReportRowsWhenInvoicedOrdersExist() {
        var p = persist(TestEntities.product("B1", "Soap", 1, 100.0, "img"));
        var o = persist(TestEntities.order(OrderStatus.INVOICED, null));
        persist(TestEntities.orderItem(o.getId(), p.getId(), 2, 10.0));
        em.clear();

        List<SalesReportData> out = dao.getSalesReportRows(LocalDate.now(), LocalDate.now(), null);

        assertFalse(out.isEmpty());
        assertEquals(2, out.get(0).getQuantity());
    }
}