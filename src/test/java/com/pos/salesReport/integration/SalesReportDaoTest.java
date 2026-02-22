package com.pos.salesReport.integration;

import com.pos.dao.SalesReportDao;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestEntities;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.SalesReportData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(SalesReportDao.class)
class SalesReportDaoTest extends AbstractDaoTest {

    @Autowired
    private SalesReportDao dao;

    @BeforeEach
    void clean() {
        em.createNativeQuery("DELETE FROM pos_order_item").executeUpdate();
        em.createNativeQuery("DELETE FROM pos_order").executeUpdate();
        em.createNativeQuery("DELETE FROM pos_product").executeUpdate();
        em.flush();
        em.clear();
    }

    @Test
    void getSalesReportRowsWhenInvoicedOrdersExist() {
        var p = persist(TestEntities.product("B1", "Soap", 1, 100.0, "img"));
        var o = persist(TestEntities.order(OrderStatus.INVOICED, null));
        persist(TestEntities.orderItem(o.getId(), p.getId(), 2, 10.0));

        em.flush();
        em.clear();

        // DAO filters by DATE(o.updated_at) (DB time is effectively UTC in tests),
        // so use UTC date to avoid IST/UTC boundary mismatches.
        LocalDate start = LocalDate.now(ZoneOffset.UTC);
        LocalDate end = start;

        List<SalesReportData> out = dao.getSalesReportRows(start, end, null);

        assertNotNull(out);
        assertFalse(out.isEmpty());
        assertEquals(2, out.get(0).getQuantity());
    }
}