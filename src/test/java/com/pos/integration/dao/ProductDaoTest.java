package com.pos.integration.dao;

import com.pos.dao.ProductDao;
import com.pos.pojo.Product;
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

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProductDao.class)
class ProductDaoTest {

    /**
     * Key fix:
     * - keep it static
     * - annotate with org.testcontainers.junit.jupiter.Container
     * - DO NOT use @TestInstance(PER_CLASS) (can lead to early property evaluation in some setups)
     * - provide driver + dialect explicitly to avoid Spring trying embedded DB paths
     */
    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("pos_test")
            .withUsername("test")
            .withPassword("test")
            .withStartupTimeout(Duration.ofMinutes(2));

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        // If container isn't started, these method refs would throw the exact error you saw.
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);

        // Make datasource resolution unambiguous
        r.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        r.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");

        // JPA schema for tests
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.jpa.show-sql", () -> "false");
        r.add("spring.jpa.properties.hibernate.format_sql", () -> "false");
    }

    @Autowired private ProductDao productDao;
    @Autowired private EntityManager em;

    @BeforeEach
    void clean() {
        // Works only if Product is an @Entity (it should be).
        em.createQuery("DELETE FROM Product").executeUpdate();
        em.flush();
        em.clear();
    }

    private Product seed(String barcode, String name, Integer clientId, Double mrp) {
        Product p = new Product();
        p.setBarcode(barcode);
        p.setName(name);
        p.setClientId(clientId);
        p.setMrp(mrp);
        p.setImageUrl("img");

        productDao.insert(p); // BaseDao method
        return p;
    }

    @Test
    void selectById_shouldReturnProduct_whenExists() {
        Product p = seed("B1", "Soap", 1, 100.0);
        em.flush();
        em.clear();

        Product found = productDao.selectById(p.getId());

        assertNotNull(found);
        assertEquals(p.getId(), found.getId());
        assertEquals("B1", found.getBarcode());
    }

    @Test
    void selectByIds_shouldReturnEmpty_whenEmptyInput() {
        assertEquals(List.of(), productDao.selectByIds(List.of()));
    }

    @Test
    void selectByIds_shouldReturnMatchingProducts() {
        Product p1 = seed("B1", "Soap", 1, 100.0);
        Product p2 = seed("B2", "Milk", 1, 50.0);
        seed("B3", "Tea", 2, 30.0);
        em.flush();
        em.clear();

        List<Product> out = productDao.selectByIds(List.of(p2.getId(), p1.getId()));

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(p -> p.getId().equals(p1.getId())));
        assertTrue(out.stream().anyMatch(p -> p.getId().equals(p2.getId())));
    }

    @Test
    void selectByBarcodes_shouldReturnEmpty_whenEmptyInput() {
        assertEquals(List.of(), productDao.selectByBarcodes(List.of()));
    }

    @Test
    void selectByBarcodes_shouldReturnMatchingProducts() {
        seed("B1", "Soap", 1, 100.0);
        seed("B2", "Milk", 1, 50.0);
        seed("B3", "Tea", 2, 30.0);
        em.flush();
        em.clear();

        List<Product> out = productDao.selectByBarcodes(List.of("B1", "B3"));

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(p -> "B1".equals(p.getBarcode())));
        assertTrue(out.stream().anyMatch(p -> "B3".equals(p.getBarcode())));
    }

    @Test
    void findProductIdsByBarcodeOrName_shouldFilterByBarcode_whenProvided() {
        Product p1 = seed("B1", "Soap", 1, 100.0);
        seed("B2", "Soap Deluxe", 1, 120.0);
        em.flush();
        em.clear();

        List<Integer> ids = productDao.findProductIdsByBarcodeOrName("B1", null);

        assertEquals(List.of(p1.getId()), ids);
    }

    @Test
    void findProductIdsByBarcodeOrName_shouldFilterByNameLike_caseInsensitive() {
        Product p1 = seed("B1", "Soap", 1, 100.0);
        Product p2 = seed("B2", "soap deluxe", 1, 120.0);
        seed("B3", "Milk", 1, 50.0);
        em.flush();
        em.clear();

        List<Integer> ids = productDao.findProductIdsByBarcodeOrName(null, "SoAp");

        assertEquals(2, ids.size());
        assertEquals(List.of(p1.getId(), p2.getId()), ids); // ORDER BY p.id
    }

    @Test
    void search_shouldFilterByNameBarcodeClientId_andPaginate() {
        seed("B1", "Soap", 1, 100.0);        // match
        seed("B2", "Soap Deluxe", 1, 120.0); // match
        seed("B3", "Soap", 2, 100.0);        // wrong client
        seed("B4", "Milk", 1, 50.0);         // wrong name
        em.flush();
        em.clear();

        List<Product> page0 = productDao.search("soap", null, 1, 0, 1);
        List<Product> page1 = productDao.search("soap", null, 1, 1, 1);

        assertEquals(1, page0.size());
        assertEquals(1, page1.size());
        assertNotEquals(page0.get(0).getId(), page1.get(0).getId());

        List<Product> byBarcode = productDao.search(null, "B2", null, 0, 10);
        assertEquals(1, byBarcode.size());
        assertEquals("B2", byBarcode.get(0).getBarcode());
    }

    @Test
    void getCount_shouldMatchSearchFilters() {
        seed("B1", "Soap", 1, 100.0);
        seed("B2", "Soap Deluxe", 1, 120.0);
        seed("B3", "Soap", 2, 100.0);
        seed("B4", "Milk", 1, 50.0);
        em.flush();
        em.clear();

        long countClient1Soap = productDao.getCount("soap", null, 1);
        assertEquals(2L, countClient1Soap);

        long countAllSoap = productDao.getCount("soap", null, null);
        assertEquals(3L, countAllSoap);

        long countBarcodeB4 = productDao.getCount(null, "B4", null);
        assertEquals(1L, countBarcodeB4);
    }
}
