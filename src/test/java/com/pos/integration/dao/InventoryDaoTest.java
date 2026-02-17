package com.pos.integration.dao;

import com.pos.dao.InventoryDao;
import com.pos.pojo.Inventory;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(InventoryDao.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InventoryDaoTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("pos_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        // ensure container started before Spring reads mapped port
        r.add("spring.datasource.url", () -> {
            if (!mysql.isRunning()) mysql.start();
            return mysql.getJdbcUrl();
        });
        r.add("spring.datasource.username", () -> {
            if (!mysql.isRunning()) mysql.start();
            return mysql.getUsername();
        });
        r.add("spring.datasource.password", () -> {
            if (!mysql.isRunning()) mysql.start();
            return mysql.getPassword();
        });

        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.jpa.show-sql", () -> "false");
        r.add("spring.jpa.properties.hibernate.format_sql", () -> "false");
    }

    @Autowired private InventoryDao inventoryDao;
    @Autowired private EntityManager em;

    @BeforeEach
    void clean() {
        em.createQuery("DELETE FROM Inventory").executeUpdate();
        em.flush();
        em.clear();
    }

    private Inventory seed(Integer productId, Integer quantity) {
        Inventory i = new Inventory();
        i.setProductId(productId);
        i.setQuantity(quantity);
        inventoryDao.insert(i);
        return i;
    }

    @Test
    void selectByProductId_shouldReturnNull_whenNotFound() {
        assertNull(inventoryDao.selectByProductId(999));
    }

    @Test
    void selectByProductId_shouldReturnInventory_whenExists() {
        Inventory i = seed(101, 5);
        seed(102, 10);
        em.flush();
        em.clear();

        Inventory out = inventoryDao.selectByProductId(101);

        assertNotNull(out);
        assertEquals(i.getId(), out.getId());
        assertEquals(101, out.getProductId());
        assertEquals(5, out.getQuantity());
    }

    @Test
    void selectByProductId_shouldReturnNull_whenProductIdNull() {
        assertNull(inventoryDao.selectByProductId(null));
    }

    @Test
    void findByProductIds_shouldReturnAll_whenNullInput_andPaginate() {
        Inventory a = seed(1, 1);
        Inventory b = seed(2, 2);
        Inventory c = seed(3, 3);
        em.flush();
        em.clear();

        List<Inventory> page0 = inventoryDao.findByProductIds(null, 0, 2);
        List<Inventory> page1 = inventoryDao.findByProductIds(null, 1, 2);

        assertEquals(2, page0.size());
        assertEquals(1, page1.size());

        // ORDER BY i.id in SELECT_ALL should make this stable
        assertEquals(a.getId(), page0.get(0).getId());
        assertEquals(b.getId(), page0.get(1).getId());
        assertEquals(c.getId(), page1.get(0).getId());
    }

    @Test
    void findByProductIds_shouldReturnAll_whenEmptyInput_andPaginate() {
        seed(1, 1);
        seed(2, 2);
        seed(3, 3);
        em.flush();
        em.clear();

        List<Inventory> out = inventoryDao.findByProductIds(List.of(), 0, 10);
        assertEquals(3, out.size());
    }

    @Test
    void findByProductIds_shouldFilter_whenProductIdsProvided_andPaginate() {
        Inventory a = seed(10, 1);
        Inventory b = seed(20, 2);
        Inventory c = seed(30, 3);
        Inventory d = seed(40, 4);
        em.flush();
        em.clear();

        // filter to {20,40}
        List<Inventory> page0 = inventoryDao.findByProductIds(List.of(20, 40), 0, 1);
        List<Inventory> page1 = inventoryDao.findByProductIds(List.of(20, 40), 1, 1);

        assertEquals(1, page0.size());
        assertEquals(1, page1.size());

        // ORDER BY i.id in SELECT_BY_PRODUCT_IDS
        assertEquals(b.getId(), page0.get(0).getId());
        assertEquals(d.getId(), page1.get(0).getId());

        assertEquals(20, page0.get(0).getProductId());
        assertEquals(40, page1.get(0).getProductId());
    }

    @Test
    void getCountByProductIds_shouldCountAll_whenNullOrEmpty() {
        seed(10, 1);
        seed(20, 2);
        seed(30, 3);
        em.flush();
        em.clear();

        assertEquals(3L, inventoryDao.getCountByProductIds(null));
        assertEquals(3L, inventoryDao.getCountByProductIds(List.of()));
    }

    @Test
    void getCountByProductIds_shouldCountOnlyMatching_whenProvided() {
        seed(10, 1);
        seed(20, 2);
        seed(30, 3);
        seed(40, 4);
        em.flush();
        em.clear();

        assertEquals(2L, inventoryDao.getCountByProductIds(List.of(10, 40)));
        assertEquals(0L, inventoryDao.getCountByProductIds(List.of(999)));
    }

    @Test
    void selectById_shouldReturnInventory_whenExists() {
        Inventory i = seed(501, 9);
        em.flush();
        em.clear();

        Inventory out = inventoryDao.selectById(i.getId());

        assertNotNull(out);
        assertEquals(i.getId(), out.getId());
        assertEquals(501, out.getProductId());
        assertEquals(9, out.getQuantity());
    }
}
