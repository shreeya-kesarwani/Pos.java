package com.pos.salesReport.integration.dao;

import com.pos.dao.OrderDao;
import com.pos.dao.OrderItemDao;
import com.pos.dao.ProductDao;
import com.pos.dao.SalesReportDao;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.SalesReportData;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({SalesReportDao.class, ProductDao.class, OrderDao.class, OrderItemDao.class})
class SalesReportDaoTest extends AbstractDaoTest {

    @Autowired
    private SalesReportDao dao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    private LocalDate todayUtc;

    @BeforeEach
    void setupData() {
        // DAO filters by DATE(o.updated_at) and DB time behaves like UTC in tests
        todayUtc = LocalDate.now(ZoneOffset.UTC);
    }

    @Test
    void getSalesReportRowsWhenInvoicedOrdersExist() {
        Product p = TestEntities.newProduct("B1", "Soap", 1, 100.0, "img");
        productDao.insert(p);

        Order o = TestEntities.newOrder(OrderStatus.INVOICED, null);
        orderDao.insert(o);

        OrderItem oi = TestEntities.newOrderItem(o.getId(), p.getId(), 2, 10.0);
        orderItemDao.insert(oi);

        em.clear();

        List<SalesReportData> out = dao.getSalesReportRows(todayUtc, todayUtc, null);

        assertNotNull(out);
        assertFalse(out.isEmpty());
        assertEquals(2, out.getFirst().getQuantity());
    }

    @Test
    void getSalesReportRows_whenNoInvoicedOrders_returnsEmpty() {
        List<SalesReportData> out = dao.getSalesReportRows(todayUtc, todayUtc, null);
        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    void getSalesReportRows_withClientIdFilter_filtersCorrectly() {
        Product p1 = TestEntities.newProduct("C1B1", "P1", 1, 100.0, "img");
        Product p2 = TestEntities.newProduct("C2B1", "P2", 2, 100.0, "img");
        productDao.insert(p1);
        productDao.insert(p2);

        Order o = TestEntities.newOrder(OrderStatus.INVOICED, null);
        orderDao.insert(o);

        OrderItem oi1 = TestEntities.newOrderItem(o.getId(), p1.getId(), 2, 10.0);
        OrderItem oi2 = TestEntities.newOrderItem(o.getId(), p2.getId(), 5, 10.0);
        orderItemDao.insert(oi1);
        orderItemDao.insert(oi2);

        em.clear();

        List<SalesReportData> outClient1 = dao.getSalesReportRows(todayUtc, todayUtc, 1);
        assertEquals(1, outClient1.size());
        assertEquals("C1B1", outClient1.getFirst().getBarcode());

        List<SalesReportData> outClient2 = dao.getSalesReportRows(todayUtc, todayUtc, 2);
        assertEquals(1, outClient2.size());
        assertEquals("C2B1", outClient2.getFirst().getBarcode());
    }
}