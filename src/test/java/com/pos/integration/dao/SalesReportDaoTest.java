package com.pos.integration.dao;

import com.pos.dao.SalesReportDao;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.SalesReportData;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(SalesReportDao.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SalesReportDaoTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("pos_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        // Ensure container started before Spring resolves datasource props
        if (!mysql.isRunning()) mysql.start();

        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);

        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.jpa.show-sql", () -> "false");
        r.add("spring.jpa.properties.hibernate.format_sql", () -> "false");

        // Optional: reduce shutdown warnings for tests
        r.add("spring.datasource.hikari.maximumPoolSize", () -> "2");
        r.add("spring.datasource.hikari.maxLifetime", () -> "30000");
    }

    @Autowired private SalesReportDao salesReportDao;
    @Autowired private EntityManager em;

    @BeforeEach
    void clean() {
        em.createQuery("DELETE FROM OrderItem").executeUpdate();
        em.createQuery("DELETE FROM Order").executeUpdate();
        em.createQuery("DELETE FROM Product").executeUpdate();
        em.flush();
        em.clear();
    }

    private Product seedProduct(String barcode, String name, int clientId) {
        Product p = new Product();
        p.setBarcode(barcode);
        p.setName(name);
        p.setClientId(clientId);
        p.setMrp(100.0);
        p.setImageUrl("img");
        em.persist(p);
        em.flush();
        return p;
    }

    private Order seedOrder(OrderStatus status, ZonedDateTime updatedAt) {
        Order o = new Order();
        o.setStatus(status); // enum setter
        em.persist(o);
        em.flush(); // get id

        // ✅ Force DB columns to match what the native SQL expects (auditing may overwrite updatedAt)
        em.createNativeQuery("""
                UPDATE pos_order
                SET status = :status,
                    updated_at = :updatedAt
                WHERE id = :id
                """)
                .setParameter("status", status.name()) // native query filters on 'INVOICED'
                .setParameter("updatedAt", Timestamp.from(updatedAt.toInstant()))
                .setParameter("id", o.getId())
                .executeUpdate();

        em.flush();
        return o;
    }

    private void seedOrderItem(int orderId, int productId, int qty, double price) {
        OrderItem oi = new OrderItem();
        oi.setOrderId(orderId);
        oi.setProductId(productId);
        oi.setQuantity(qty);
        oi.setSellingPrice(price);
        em.persist(oi);
    }

    @Test
    void getSalesReportRows_shouldReturnEmpty_whenNoInvoicedOrdersInRange() {
        Product p = seedProduct("B1", "Soap", 1);
        Order o = seedOrder(OrderStatus.CREATED, ZonedDateTime.now(ZONE));
        seedOrderItem(o.getId(), p.getId(), 2, 10.0);

        em.flush();
        em.clear();

        List<SalesReportData> out = salesReportDao.getSalesReportRows(
                LocalDate.now(ZONE),
                LocalDate.now(ZONE),
                null
        );

        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    void getSalesReportRows_shouldAggregateQuantityAndRevenue_forInvoicedOrdersInDateRange() {
        Product p1 = seedProduct("B1", "Soap", 1);
        Product p2 = seedProduct("B2", "Milk", 1);

        ZonedDateTime d1 = ZonedDateTime.of(2026, 2, 10, 10, 0, 0, 0, ZONE);
        ZonedDateTime d2 = ZonedDateTime.of(2026, 2, 11, 11, 0, 0, 0, ZONE);

        Order o1 = seedOrder(OrderStatus.INVOICED, d1);
        Order o2 = seedOrder(OrderStatus.INVOICED, d2);

        seedOrderItem(o1.getId(), p1.getId(), 2, 10.0);
        seedOrderItem(o1.getId(), p2.getId(), 1, 20.0);
        seedOrderItem(o2.getId(), p1.getId(), 3, 10.0);

        em.flush();
        em.clear();

        List<SalesReportData> out = salesReportDao.getSalesReportRows(
                LocalDate.of(2026, 2, 10),
                LocalDate.of(2026, 2, 11),
                null
        );

        assertEquals(2, out.size());

        SalesReportData soap = out.stream()
                .filter(r -> "B1".equals(r.getBarcode()))
                .findFirst()
                .orElseThrow();

        assertEquals("Soap", soap.getProductName());
        assertEquals(5, soap.getQuantity());
        assertEquals(50.0, soap.getRevenue(), 0.0001);

        SalesReportData milk = out.stream()
                .filter(r -> "B2".equals(r.getBarcode()))
                .findFirst()
                .orElseThrow();

        assertEquals("Milk", milk.getProductName());
        assertEquals(1, milk.getQuantity());
        assertEquals(20.0, milk.getRevenue(), 0.0001);
    }

    @Test
    void getSalesReportRows_shouldExcludeOrdersOutsideRange() {
        Product p = seedProduct("B1", "Soap", 1);

        ZonedDateTime inRange = ZonedDateTime.of(2026, 2, 10, 10, 0, 0, 0, ZONE);
        ZonedDateTime outRange = ZonedDateTime.of(2026, 2, 12, 10, 0, 0, 0, ZONE);

        Order o1 = seedOrder(OrderStatus.INVOICED, inRange);
        Order o2 = seedOrder(OrderStatus.INVOICED, outRange);

        seedOrderItem(o1.getId(), p.getId(), 2, 10.0);
        seedOrderItem(o2.getId(), p.getId(), 100, 10.0);

        em.flush();
        em.clear();

        List<SalesReportData> out = salesReportDao.getSalesReportRows(
                LocalDate.of(2026, 2, 10),
                LocalDate.of(2026, 2, 11),
                null
        );

        assertEquals(1, out.size());
        assertEquals("B1", out.get(0).getBarcode());
        assertEquals(2, out.get(0).getQuantity());
        assertEquals(20.0, out.get(0).getRevenue(), 0.0001);
    }

    @Test
    void getSalesReportRows_shouldFilterByClientId() {
        Product pClient1 = seedProduct("B1", "Soap", 1);
        Product pClient2 = seedProduct("B2", "Milk", 2);

        ZonedDateTime d = ZonedDateTime.of(2026, 2, 10, 10, 0, 0, 0, ZONE);
        Order o = seedOrder(OrderStatus.INVOICED, d);

        seedOrderItem(o.getId(), pClient1.getId(), 2, 10.0);
        seedOrderItem(o.getId(), pClient2.getId(), 3, 10.0);

        em.flush();
        em.clear();

        List<SalesReportData> outClient1 = salesReportDao.getSalesReportRows(
                LocalDate.of(2026, 2, 10),
                LocalDate.of(2026, 2, 10),
                1
        );
        assertEquals(1, outClient1.size());
        assertEquals("B1", outClient1.get(0).getBarcode());
        assertEquals(2, outClient1.get(0).getQuantity());
        assertEquals(20.0, outClient1.get(0).getRevenue(), 0.0001);

        List<SalesReportData> outClient2 = salesReportDao.getSalesReportRows(
                LocalDate.of(2026, 2, 10),
                LocalDate.of(2026, 2, 10),
                2
        );
        assertEquals(1, outClient2.size());
        assertEquals("B2", outClient2.get(0).getBarcode());
        assertEquals(3, outClient2.get(0).getQuantity());
        assertEquals(30.0, outClient2.get(0).getRevenue(), 0.0001);
    }
}
