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
}