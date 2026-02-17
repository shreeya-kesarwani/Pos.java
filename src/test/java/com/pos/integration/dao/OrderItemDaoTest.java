package com.pos.integration.dao;

import com.pos.dao.OrderItemDao;
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
import java.util.Arrays;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(OrderItemDao.class)
class OrderItemDaoTest {

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

        // keeps ZonedDateTime stable across MySQL if any entity uses it elsewhere
        r.add("spring.jpa.properties.hibernate.jdbc.time_zone", () -> "UTC");
    }

    @Autowired private OrderItemDao orderItemDao;
    @Autowired private EntityManager em;

    @BeforeEach
    void clean() {
        // project tables are typically prefixed with pos_
        // and order_item likely depends on order, so delete child first.
        em.createNativeQuery("DELETE FROM pos_order_item").executeUpdate();
        em.flush();
        em.clear();
    }

    private OrderItem seed(Integer orderId, Integer productId, int qty, double sp) {
        OrderItem oi = new OrderItem();
        oi.setOrderId(orderId);
        oi.setProductId(productId);
        oi.setQuantity(qty);
        oi.setSellingPrice(sp);

        em.persist(oi);
        em.flush();
        em.clear();
        return oi;
    }

    @Test
    void selectByOrderId_shouldReturnEmpty_whenNoRows() {
        List<OrderItem> out = orderItemDao.selectByOrderId(999);
        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    void selectByOrderId_shouldReturnOnlyItemsForThatOrder() {
        // order 10 -> 2 items
        OrderItem a = seed(10, 101, 2, 50.0);
        OrderItem b = seed(10, 102, 1, 20.0);

        // order 11 -> 1 item (should NOT come back)
        seed(11, 103, 5, 10.0);

        List<OrderItem> out = orderItemDao.selectByOrderId(10);

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(a.getId())));
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(b.getId())));
        assertTrue(out.stream().allMatch(x -> x.getOrderId().equals(10)));
    }

    @Test
    void selectByOrderId_shouldNotLeakOtherOrders() {
        seed(1, 201, 1, 5.0);
        seed(2, 202, 1, 5.0);
        seed(3, 203, 1, 5.0);

        List<OrderItem> out = orderItemDao.selectByOrderId(2);

        assertEquals(1, out.size());
        assertEquals(2, out.get(0).getOrderId());
    }

    @Test
    void selectByOrderIds_shouldReturnEmpty_whenNullOrEmptyInput() {
        List<OrderItem> out1 = orderItemDao.selectByOrderIds(null);
        assertNotNull(out1);
        assertTrue(out1.isEmpty());

        List<OrderItem> out2 = orderItemDao.selectByOrderIds(List.of());
        assertNotNull(out2);
        assertTrue(out2.isEmpty());
    }

    @Test
    void selectByOrderIds_shouldReturnItemsForAllGivenOrders_only() {
        // order 10 -> 2 items
        OrderItem a = seed(10, 101, 2, 50.0);
        OrderItem b = seed(10, 102, 1, 20.0);

        // order 11 -> 1 item
        OrderItem c = seed(11, 103, 5, 10.0);

        // order 12 -> should NOT come back
        seed(12, 104, 1, 99.0);

        List<OrderItem> out = orderItemDao.selectByOrderIds(Arrays.asList(10, 11));

        assertEquals(3, out.size());

        // contains all expected
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(a.getId())));
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(b.getId())));
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(c.getId())));

        // and no leakage
        assertTrue(out.stream().allMatch(x -> x.getOrderId().equals(10) || x.getOrderId().equals(11)));
    }

    @Test
    void selectByOrderIds_shouldHandleDuplicateOrderIds() {
        OrderItem a = seed(20, 201, 1, 10.0);
        OrderItem b = seed(21, 202, 1, 10.0);

        // duplicates in input
        List<OrderItem> out = orderItemDao.selectByOrderIds(List.of(20, 20, 21));

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(a.getId())));
        assertTrue(out.stream().anyMatch(x -> x.getId().equals(b.getId())));
    }

}
