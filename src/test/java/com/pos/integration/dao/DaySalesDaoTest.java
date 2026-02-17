package com.pos.integration.dao;

import com.pos.dao.DaySalesDao;
import com.pos.model.constants.OrderStatus;
import com.pos.pojo.DaySales;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(DaySalesDao.class)
class DaySalesDaoTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("pos_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);

        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.jpa.show-sql", () -> "false");
        r.add("spring.jpa.properties.hibernate.format_sql", () -> "false");

        // ✅ THIS is the important fix (stabilizes ZonedDateTime <-> MySQL comparisons)
        r.add("spring.jpa.properties.hibernate.jdbc.time_zone", () -> "UTC");
    }

    @Autowired private DaySalesDao daySalesDao;
    @Autowired private EntityManager em;

    @BeforeEach
    void clean() {
        // project tables are prefixed with pos_
        em.createNativeQuery("DELETE FROM pos_order_item").executeUpdate();
        em.createNativeQuery("DELETE FROM pos_order").executeUpdate();
        em.createNativeQuery("DELETE FROM pos_day_sales").executeUpdate();
        em.flush();
        em.clear();
    }

    private static Timestamp tsUtc(ZonedDateTime zdt) {
        return Timestamp.from(zdt.toInstant());
    }

    private Order seedOrder(OrderStatus status, ZonedDateTime logicalTimeUtc) {
        Order o = new Order();
        o.setStatus(status);
        o.setInvoicePath("inv");

        em.persist(o);
        em.flush();

        // force timestamps in DB (because @CreationTimestamp/@UpdateTimestamp overwrite entity fields)
        em.createNativeQuery("""
                UPDATE pos_order
                   SET created_at = :t,
                       updated_at = :t
                 WHERE id = :id
                """)
                .setParameter("t", tsUtc(logicalTimeUtc))
                .setParameter("id", o.getId())
                .executeUpdate();

        em.flush();
        em.clear();
        return o;
    }

    private void seedOrderItem(Integer orderId, Integer productId, int qty, double sp) {
        OrderItem oi = new OrderItem();

        // assuming your OrderItem has these setters (your DaySalesDao uses them)
        oi.setOrderId(orderId);
        oi.setProductId(productId);
        oi.setQuantity(qty);
        oi.setSellingPrice(sp);

        em.persist(oi);
        em.flush();
        em.clear();
    }

    private DaySales seedDaySales(ZonedDateTime dayUtc, int orders, int items, double revenue) {
        DaySales ds = new DaySales();
        ds.setDate(dayUtc);
        ds.setInvoicedOrdersCount(orders);
        ds.setInvoicedItemsCount(items);
        ds.setTotalRevenue(revenue);

        em.persist(ds);
        em.flush();
        em.clear();
        return ds;
    }

    @Test
    void selectInRange_shouldReturnOnlyRowsWithinRange() {
        ZonedDateTime base = ZonedDateTime.now(ZoneId.of("UTC")).withHour(0).withMinute(0).withSecond(0).withNano(0);

        seedDaySales(base.minusDays(2), 1, 2, 100.0);
        DaySales in1 = seedDaySales(base.minusDays(1), 2, 3, 200.0);
        DaySales in2 = seedDaySales(base,           3, 4, 300.0);
        seedDaySales(base.plusDays(1),  4, 5, 400.0);

        ZonedDateTime start = base.minusDays(1);
        ZonedDateTime end   = base.plusDays(1); // end is exclusive in DAO query

        List<DaySales> out = daySalesDao.selectInRange(start, end);

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(d -> d.getId().equals(in1.getId())));
        assertTrue(out.stream().anyMatch(d -> d.getId().equals(in2.getId())));
    }

    @Test
    void selectInvoicedSalesAggregatesForDay_shouldAggregateOnlyInvoicedOrdersWithinRange() {
        ZonedDateTime base = ZonedDateTime.now(ZoneId.of("UTC")).withNano(0);

        // in range
        Order invoicedIn = seedOrder(OrderStatus.INVOICED, base.minusMinutes(10));
        seedOrderItem(invoicedIn.getId(), 101, 2, 50.0); // qty=2 revenue=100

        // in range but NOT invoiced -> excluded
        Order createdIn = seedOrder(OrderStatus.CREATED, base.minusMinutes(9));
        seedOrderItem(createdIn.getId(), 102, 5, 10.0);

        // invoiced but OUT of range -> excluded
        Order invoicedOut = seedOrder(OrderStatus.INVOICED, base.minusDays(2));
        seedOrderItem(invoicedOut.getId(), 103, 1, 999.0);

        ZonedDateTime start = base.minusHours(1);
        ZonedDateTime end   = base.plusHours(1);

        Object[] agg = daySalesDao.selectInvoicedSalesAggregatesForDay(start, end);

        long ordersCount = ((Number) agg[0]).longValue();
        long itemsCount  = ((Number) agg[1]).longValue();
        double revenue   = ((Number) agg[2]).doubleValue();

        assertEquals(1L, ordersCount);     // ✅ ONLY invoicedIn
        assertEquals(2L, itemsCount);      // qty 2
        assertEquals(100.0, revenue, 0.0001);
    }
}
