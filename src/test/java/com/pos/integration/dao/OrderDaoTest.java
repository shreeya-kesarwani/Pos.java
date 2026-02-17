package com.pos.integration.dao;

import com.pos.dao.OrderDao;
import com.pos.model.constants.OrderStatus;
import com.pos.pojo.Order;
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
@Import(OrderDao.class)
class OrderDaoTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("pos_test")
            .withUsername("test")
            .withPassword("test")
            // IMPORTANT: lock MySQL server timezone to UTC for stable comparisons
            .withCommand("--default-time-zone=+00:00");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);

        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.jpa.show-sql", () -> "false");
        r.add("spring.jpa.properties.hibernate.format_sql", () -> "false");

        // IMPORTANT: make Hibernate bind timestamps in UTC
        r.add("spring.jpa.properties.hibernate.jdbc.time_zone", () -> "UTC");
    }

    @Autowired private OrderDao orderDao;
    @Autowired private EntityManager em;

    @BeforeEach
    void clean() {
        // ensure session is UTC too
        em.createNativeQuery("SET time_zone = '+00:00'").executeUpdate();

        em.createNativeQuery("DELETE FROM pos_order").executeUpdate();
        em.flush();
        em.clear();
    }

    private static Timestamp ts(ZonedDateTime zdt) {
        return Timestamp.from(zdt.toInstant()); // absolute instant
    }

    private Order seed(OrderStatus status, ZonedDateTime logicalTime) {
        Order o = new Order();
        o.setStatus(status);
        o.setInvoicePath("inv");

        em.persist(o);
        em.flush(); // ID generated

        // Force DB columns, because @CreationTimestamp/@UpdateTimestamp overwrite Java fields
        em.createNativeQuery("""
                UPDATE pos_order
                   SET created_at = :t,
                       updated_at = :t
                 WHERE id = :id
                """)
                .setParameter("t", ts(logicalTime))
                .setParameter("id", o.getId())
                .executeUpdate();

        em.flush();
        em.clear();
        return o;
    }

    @Test
    void search_shouldFilterByStatus() {
        ZonedDateTime base = ZonedDateTime.now(ZoneId.of("UTC"));

        seed(OrderStatus.CREATED,  base.minusHours(3));
        Order invoiced = seed(OrderStatus.INVOICED, base.minusHours(2));
        seed(OrderStatus.CREATED,  base.minusHours(1));

        List<Order> out = orderDao.search(null, null, null, OrderStatus.INVOICED, 0, 50);

        assertEquals(1, out.size());
        assertEquals(invoiced.getId(), out.get(0).getId());
        assertEquals(OrderStatus.INVOICED, out.get(0).getStatus());
    }

    @Test
    void search_shouldFilterByDateRange_onUpdatedAt() {
        ZonedDateTime base = ZonedDateTime.now(ZoneId.of("UTC"));

        seed(OrderStatus.INVOICED, base.minusDays(3));              // outside
        Order in1 = seed(OrderStatus.INVOICED, base.minusDays(1));  // inside
        Order in2 = seed(OrderStatus.CREATED,  base.minusHours(6)); // inside
        seed(OrderStatus.CREATED,  base.plusDays(2));               // outside

        ZonedDateTime start = base.minusDays(2);
        ZonedDateTime end   = base.minusHours(1);

        List<Order> out = orderDao.search(null, start, end, null, 0, 50);

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(o -> o.getId().equals(in1.getId())));
        assertTrue(out.stream().anyMatch(o -> o.getId().equals(in2.getId())));
    }

    @Test
    void search_shouldPaginate_andSortByUpdatedAtDesc() {
        ZonedDateTime base = ZonedDateTime.now(ZoneId.of("UTC"));

        Order newest = seed(OrderStatus.CREATED, base.minusMinutes(1));
        seed(OrderStatus.CREATED, base.minusMinutes(2));
        seed(OrderStatus.CREATED, base.minusMinutes(3));

        List<Order> page0 = orderDao.search(null, null, null, null, 0, 1);
        List<Order> page1 = orderDao.search(null, null, null, null, 1, 1);

        assertEquals(1, page0.size());
        assertEquals(1, page1.size());
        assertEquals(newest.getId(), page0.get(0).getId());
        assertNotEquals(page0.get(0).getId(), page1.get(0).getId());
    }

    @Test
    void getCount_shouldMatchFilters() {
        ZonedDateTime base = ZonedDateTime.now(ZoneId.of("UTC"));

        seed(OrderStatus.INVOICED, base.minusDays(1));
        seed(OrderStatus.INVOICED, base.minusHours(2));
        seed(OrderStatus.CREATED,  base.minusHours(1));

        long all = orderDao.getCount(null, null, null, null);
        assertEquals(3L, all);

        long invoicedOnly = orderDao.getCount(null, null, null, OrderStatus.INVOICED);
        assertEquals(2L, invoicedOnly);

        ZonedDateTime start = base.minusHours(3);
        ZonedDateTime end   = base.minusMinutes(30);

        long inRange = orderDao.getCount(null, start, end, null);
        assertEquals(2L, inRange);
    }
}
