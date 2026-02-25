package com.pos.salesReport.integration.dao;

import com.pos.dao.SalesReportDao;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.SalesReportData;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({SalesReportDao.class, TestFactory.class})
class SalesReportDaoTest extends AbstractDaoTest {

    @Autowired
    private SalesReportDao dao;

    @Autowired
    private TestFactory testFactory;

    private LocalDate todayUtc;

    @BeforeEach
    void setupData() {
        // DAO filters by DATE(o.updated_at) and DB time behaves like UTC in tests
        todayUtc = LocalDate.now(ZoneOffset.UTC);
    }

    @Test
    void getSalesReportRowsWhenInvoicedOrdersExist() {
        // If client FK exists, create a client and use its id. If not, clientId=1 is fine.
        Product p = testFactory.createProduct("B1", "Soap", 1, 100.0, "img");
        Order o = testFactory.createOrder(OrderStatus.INVOICED, null);

        OrderItem oi = new OrderItem();
        oi.setProductId(p.getId());
        oi.setQuantity(2);
        oi.setSellingPrice(10.0);
        testFactory.createOrderItems(o.getId(), List.of(oi));

        em.flush();
        em.clear();

        List<SalesReportData> out = dao.getSalesReportRows(todayUtc, todayUtc, null);

        assertNotNull(out);
        assertFalse(out.isEmpty());
        assertEquals(2, out.getFirst().getQuantity());
    }

    @Test
    void getSalesReportRows_whenNoInvoicedOrders_returnsEmpty() {
        // no invoiced orders inserted
        List<SalesReportData> out = dao.getSalesReportRows(todayUtc, todayUtc, null);
        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    void getSalesReportRows_withClientIdFilter_filtersCorrectly() {
        // create 2 products with different clientIds
        Product p1 = testFactory.createProduct("C1B1", "P1", 1, 100.0, "img");
        Product p2 = testFactory.createProduct("C2B1", "P2", 2, 100.0, "img");

        Order o = testFactory.createOrder(OrderStatus.INVOICED, null);

        OrderItem oi1 = new OrderItem();
        oi1.setProductId(p1.getId());
        oi1.setQuantity(2);
        oi1.setSellingPrice(10.0);

        OrderItem oi2 = new OrderItem();
        oi2.setProductId(p2.getId());
        oi2.setQuantity(5);
        oi2.setSellingPrice(10.0);

        testFactory.createOrderItems(o.getId(), List.of(oi1, oi2));
        em.flush(); em.clear();

        List<SalesReportData> outClient1 = dao.getSalesReportRows(todayUtc, todayUtc, 1);
        assertEquals(1, outClient1.size());
        assertEquals("C1B1", outClient1.getFirst().getBarcode());

        List<SalesReportData> outClient2 = dao.getSalesReportRows(todayUtc, todayUtc, 2);
        assertEquals(1, outClient2.size());
        assertEquals("C2B1", outClient2.getFirst().getBarcode());
    }
}